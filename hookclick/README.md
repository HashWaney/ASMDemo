## 原理
   
    在注册的Application.ActivityLifecycleCallbacks的onActivityResumed(Activity activity)回调方法中，
   
   **  获取的View对象是谁？？？ **
    
    通过activity.getWindow().getDecorView()获取DecorView
    Why DecorView， is Not ContentView(Activity#setContentView) 主要是考虑到MenuItem的点击事件
    
    
    
   ** 遍历DecorView，判断当前的View是否设置了OnClickListener,如果已经设置了，怎么办？？？**
    
    首先我们通过定一个自己的WrapOnClickListener来对OnClickListener进行包装。
    即WrapOnClickListener中的onClick方法会调用OnClickListener处理逻辑。
    
    
   ** 动态添加的View如何拦截？？？**
  
    引入ViewTreeObserver.OnGlobalLayoutListener来监听视图树的变换
    而且要在Activity#onStop中移除removeOnGlobalLayoutListener
   
    
## 实现

   1、创建代理类WrapClickListener 
     
```java
            import android.util.Log;import android.view.View;
            
            public class WrapClickListener implements View.OnClickListener{
                
                public static final String TAG ="WrapClickListener";
                
                private View.OnClickListener originClick;
                
                @Override
                public void onClick(View view){
                    if (originClick!=null){
                        Log.i(TAG,"onClick before");
                        originClick.onClick(v);
                        Log.i(TAG,"onClick after");
                    }
                }       
                    
    
            }       
    
    
  ```
        
   2、代理OnClickListener事件
       
```java
            
    import java.lang.reflect.Field;
    import java.lang.reflect.Method;
    import android.view.View;
    public class HookClickHelper{
             
     public static final String TAG = "HookClickHelper";
                
     public static void hookClick(View view) throws  Exception{
        //1.反射得到ListenerInfo对象  思考🤔：：ListenerInfo包含了Listener的相关信息，长按事件onLongClick  普通点击事件onClick 等等
        Method getListenerInfo = View.class.getDeclaredMethod("getListenerInfo");
         //设置该方法可以被访问
         getListenerInfo.setAccessible(true);   
          //view.mListenerInfo;  拿不到是不是啊
         //通过方法来获取所不能访问的ListenerInfo对象
         // 其实这一步可以看作view.getListenerInfo =>> ListenerInfo 
          // 只不过反射是一种逆过程获取
           Object listenerInfo =getListenerInfo.invoke(view);
    
    
                    //2.得到原始的OnClickListener事件方法
                    Class<?> listenerInfoClazz = Class.forName("android.view.View$ListenerInfo");
                    Field mOnClickListener  = listenerInfoClazz.getDeclaredField("mOnClickListener");
                    mOnClickListener.setAccessible(true);
                   //根据上面获取的ListenerInfo对象来获取其对应的OnClickListener对象
                   // 这里也是一种一种逆过程，已经拿到mOnClickListener 作用变量 但是真实的对象是listenerInfo
                   // 因此要获取真实的view传递过来的onClickListener需要通过上面第一步获取的view对应的ListenerInfo来获取真实的
                   // OnClickListener ---> field.get(obj) 也是一个逆向过程，其实就是obj.get(...)--->field
                    View.OnClickListener originOnClickListener = mOnClickListener.get(ListenerInfo);
                    if(originOnClickListener==null || originOnClickListener instanceof WrapClickListener){
                        // 如果没有设置点击事件或者已经代理过了，则跳过
                            return;
                    }
                    
                   //3。替换
                    WrapClickListener proxy = new WrapClickListener(originOnClickListener);
                    mOnClickListener.set(listenerInfo,proxy);
            
                }
                
            
            }           
           
```
         
   3、在Activity完全显示的时候遍历View动态代理OnClickListener
   
        
```java
            
          import android.app.Application;
          import android.view.View;
          import android.view.ViewGroup;
          import android.view.ViewTreeObserver;
                
          import android.app.ActivityLifecycleCallbacks;
              
           public class MyApplication extends Application{
              
                
              public static final String TAG ="MyApp";
                
                 @Override
                 public void onCreate(){
                    super.onCreate();
                    this.registerActivityLifecycleCallbacks(new MyActivityLifecycleCallback());
                 }   
                   
                static class MyActivityLifecycleCallback implements  ActivityLifecycleCallbacks {
                    
                    private View mDecorView;
                    public ViewTreeObserver.OnGlobalLayoutListener mOnGlobalLayoutListener = new ViewTreeObserver.OnGlobalLayoutListener() {
                    
                        @Override
                        public void onGlobalLayout(){
                            // 对于动态添加的view设置动态代理
                            setAllViewsProxy((ViewGroup)mDecorView);
                        }     
                
        
                    };
                          @Override
                           public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
                           }
                   
                           @Override
                           public void onActivityStarted(Activity activity) {
                           }
                   
                           @Override
                           public void onActivityResumed(Activity activity) {
                               mDecorView = activity.getWindow().getDecorView();
                               setAllViewsProxy((ViewGroup) mDecorView);
                           }
                   
                           @Override
                           public void onActivityPaused(Activity activity) {
                           }
                   
                           @Override
                           public void onActivityStopped(Activity activity) {
                               mDecorView.getViewTreeObserver().removeGlobalOnLayoutListener(mOnGlobalLayoutListener);
                           }
                   
                           @Override
                           public void onActivitySaveInstanceState(Activity activity, Bundle outState) {
                           }
                   
                           @Override
                           public void onActivityDestroyed(Activity activity) {
                   
                           }         
            
                        private void setAllViewsProxy(ViewGroup viewsProxy){
                            int childCount = viewsProxy.getChildCount();
                            for(int i = 0; i < childCount; i++) {
                              View view = viewsProxy.getChildAt(i);
                              if (view instanceof ViewGroup){
                                    setAllViewsProxy(((ViewGroup)view));
                              }else{
                                 try{
                                      if (view.hasOnClickListeners()){
                                         HookClickHelper.hookClick(view);
                                     }
                                 }catch (Exception e){
                                     e.printStackTrace();
                                 }
                                 
                              }
                            }
                        }        
                }       
               
              }       
 ```
            
  
## 优缺点

 优点： 动态性，可以监测到动态添加的view
 缺点：
    1。使用了反射，对APP整体性能🈶影响，也可能带来兼容性问题
    2。无法采集游离在Activity之上的view的点击，比如Dialog，PopupWindow
    3。OnGlobalLayoutListener API16+  
    

## 参考
   
  https://github.com/Omooo/Android-Notes
   
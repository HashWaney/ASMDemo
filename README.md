## 背景
    @ https://www.jianshu.com/p/0a56e151e00b
    实际开发过程中，对于一些业务代码有一些统计要求，我们想要的不侵入代码，
    完成对一些统计，比如说无埋点方案。
    
    该Demo是一个统计方法耗时的演示。比如统计页面停留的时间。
    
    
    目的：
        - 不影响现有的逻辑
        - 需要统计的耗时的方法头部加上注解
        - 支持混淆
        
        
        
    方案：
        针对JVM
           自定义注解》 编译时期干点事
           使用ASM增加字节码》对原有的字节码加上统计耗时的代码，修改其方法
           反射实例化？？？拿到修改之后的二进制字节流生成.class文件，通过反射实例化，实例化之后调用其方法，
           使用Instrumentation构建代理？？？Jvm执行main函数之前，实现一个代理，这个代理可以对
           载入到到JVM的正常的字节码文件进行修改，修改完成之后然后在传给JVM，完成加载。
           
           
        针对Android
            gradle plugin 自定义 Transform Api （第一个执行Task）
            Transform API 允许第三方插件在class文件转换为dex文件前操作编译好的class文件。
           
           /build/intermediates/classes/release/..class 
            
            
            
           
           
           
           
        
        
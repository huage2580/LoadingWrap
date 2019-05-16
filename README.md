# LoadingWrap
为textview，button，添加loading效果

## simple code
```kotlin

  //static 方式
  LoadingWrap.toLoadingStatus(button,LoadingWrap.SIZE.SMALL,true)
        button.setOnClickListener {
        //扩展函数的方式
            button.toStringStatus()
        }
```

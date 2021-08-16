# AppInject

跨进程 Activity 管理库，可以对 activity 进行批量操作

## Use

```gradle
implementation("io.github.clistery:appinject:1.4.3")
implementation("io.github.clistery:actmanager:1.2.2")
```

```kotlin
class DemoApp : Application(), IBaseAppInject {
    override fun onCreate() {
        super.onCreate()

        ActManager.get().register(this)
    }
}
```

## API

- IForegroundEvent
  - APP 前/后台状态监听器
- IActStatusEvent
  - Activity状态监听器
- enableForcedStackTopMode
  - 是否开启强制栈顶模式（当 APP 进入后台后会自动记录栈顶 activity，在重新打开 APP 时会自动恢复到栈顶 activity）
  - 使用情形：
    - 当前栈打开顺序为 A(normal) -> B(singleInstance)
    - 当进入后台时栈顶为 B，默认情况下，重新打开 APP 时会回到 A，开启该功能后，会回到 B

- killAll
  - 关闭进程的所有 activity
- killAllByProcess
  - 关闭当前进程所有 activity
- topAct
  - 获取当前进程的栈顶 activity
- finishForceGoBack
  - 强制按打开顺序返回
- finishAndOpenPreTask
  - 关闭当前 activity 并尝试恢复上一个堆栈到前台

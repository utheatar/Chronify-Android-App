# Chronify

todo

- [x] DTText文本样式优化
- [x] 将侧拉导航改为底部导航
- [x] Journal Item 长title显示问题
- [ ] 统计
    - [ ] 日历统计：事件在可滚动的日历上显现
      - [x] 仿GitHub提交活跃记录，在日历上以颜色浓度表示当日事件的数量，点击日期后显示事件列表。有筛选功能，可根据title筛选事件。
      - [ ] 添加日期跳转。
    - [ ] 图表统计：查询指定title的事件，统计其数量，以图表形式展示
- [ ] 设置
    - [x] 导出记录数据为csv或其他格式文件，目前已经找到了写入csv文件的方法，还差数据转换。
    - [x] 设置用户偏好选项
    - [ ] 自定义快捷添加事件
- [ ] 优化日期选择，添加initial参数。若不为null，默认选中已有日期（该功能优先级不高）

## Introduction

This is a simple schedule Android App that allows you to add, delete, and view your task/reminder/todo/check. The APP is written in Kotlin with Jetpack Compose UI and uses the Room database to store data.

Many functions are developing, basic adding, deleting and editing is completed.

## Installation





## Manual

Schedule type: CYCLICAL, REMINDER, MISSION, CHECK_IN, DEFAULT

| Schedule Type | Description                        | non-null Attributes | nullable Attributes  |
|---------------| ---------------------------------- | ------------------- | -------------------- |
| REMINDER      | Reminds you at a specific time     | end                 | begin, interval      |
| RECORD        | Check in at a specific time        | begin/end           | begin/end, interval  |
| CYCLICAL      | Repeats every day at the same time | begin, interval     | begin, end           |
| DEFAULT       | A default schedule                 |                     | begin, end, interval |

## Project structure

### Data Layer

The schedule data are stored in a Room database. Schedule items are represented by the `Schedule` class and queries on the data table are made by the `ScheduleDao` class. The app includes some view model to access the `ScheduleDao` and format data to be display to users.

定义接口 ItemsRepository 并用 OfflineItemsRepository 实现它有以下几个好处：  
- 解耦：接口将具体实现与使用者解耦，使得代码更灵活。你可以在不改变使用者代码的情况下，轻松替换 OfflineItemsRepository 的实现。  
- 可测试性：使用接口可以更容易地编写单元测试。你可以创建一个 ItemsRepository 的模拟实现来测试依赖它的代码，而不需要依赖实际的数据库操作。  
- 扩展性：如果将来需要添加其他数据源（例如网络数据源），你只需创建一个新的实现类（例如 OnlineItemsRepository），而不需要修改现有的代码。  
- 遵循SOLID原则：接口和实现分离符合面向对象设计中的SOLID原则，特别是依赖倒置原则（DIP），这有助于创建更健壮和可维护的代码。 
通过这种方式，你的代码将更具灵活性、可维护性和可测试性。

### UI Layer

# For developers

1. Install Android Studio, if you don't already have it.
2. Download the sample.
3. Import the sample into Android Studio.
4. Build and run the sample.

# References

- [Android Basics with Compose](https://developer.android.com/courses/android-basics-compose/course)
- [AnchoredDraggable Modifier](https://canopas.com/how-to-implement-swipe-to-action-using-anchoreddraggable-in-jetpack-compose-cccb22e44dff)
- [ComposeMultiDatePicker](https://github.com/playmoweb/ComposeMultiDatePicker)
- [ComposeDatePicker](https://github.com/vsnappy1/ComposeDatePicker)
- [【自荐+送码】个人新作品：Aphrodite——性生活日历](https://meta.appinn.net/t/topic/26613)
- [少数派](https://sspai.com/post/70238)

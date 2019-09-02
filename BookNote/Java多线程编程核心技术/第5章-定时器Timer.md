# 第 5 章 定时器 Timer

## 本章主要内容
	

## 5.1 定时器 Timer 的使用
　　

#### 5.1.1 方法 schedule (TimerTask,Date time)的测试
　　

###### 1.执行任务的时间晚于当前时间：在未来执行的效果
　　


###### 2.计划时间早于当前时间：提前运行的效果
　　

###### 3.多个 TimerTask 任务及延时的测试
　　

#### 5.1.2 方法 schedule(TimerTask task,Date firstTime,long period)的测试
　　


###### 1.计划时间晚于当前时间：在未来执行的效果
　　

###### 2.计划时间早于当前时间：提前运行的效果
　　

###### 3. 任务执行时间被延时
　　

###### 4. TimerTask 类的 cancel() 方法
　　

###### 5. Timer 类的 cancel() 方法
　　


###### 6. TImer 的 cancel() 方法注意事项
　　


#### 5.1.3 方法 schedule(TimerTask task,long delay) 的测试
　　该方法的作用是以执行 schedule(TimerTask task,long delay) 方法当前的时间为参考时间，在此时间基础上延迟指定的毫秒数后执行一次 TimerTask 任务。

#### 5.1.4 方法 schedule(TimerTask task,long delay,long period) 的测试
　　该方法的作用是以执行 schedule(TimerTask task,long delay,long period) 方法当前的时间为参考时间，在此时间基础上延迟执行的毫秒数，再以某一间隔时间无限次数地执行某一任务。

　　凡是使用方法中带有 period 参数的，都是无限循环执行 TimerTask 中的任务。

#### 5.1.5 方法 scheduleAtFixedRate(TimerTask task,Date firstTime,long period) 的测试
　　方法 schedule 和方法 scheduleAtFixedRate 都会按顺序执行，所以不要考虑非线程安全的情况。

　　方法 schedule 和 scheduleAtFixedRate 主要的区别只在于不延时的情况。

　　使用 schedule 方法：如果执行任务的时间没有被延时，那么下一次任务的执行时间参考的是上一次任务的“开始”时的时间来计算。

　　使用 scheduleAtFixedRate 方法：如果执行任务的时间没有被延时，那么下一次任务的执行时间参考的是上一次任务的“结束”时的时间来计算。

　　延时的情况则没有区别，也就是使用 schedule 或 scheduleAtFixedRate 方法都是如果执行任务的时间被延时，那么下一次任务的执行时间参考的是上一次任务“结束”时的时间来计算。

###### 1.测试 schedule 方法任务不延时
　　在不延时的情况下，如果执行任务的时间没有被延时（执行任务的时间小于 delay 的时间），则下一次执行任务的时间是上一次任务的开始时间加上 delay 时间。

###### 2.测试 schedule 方法任务延时
　　如果执行任务的时间被延时（执行任务的时间大于 delay 的时间），那么下一次任务的执行时间以上一次任务“结束”时的时间为参考来计算。

###### 3.测试 scheduleAtFixedRate 方法任务不延时
　　如果执行任务的时间没有被延时（执行任务的时间小于 delay 的时间），则下一次认知性任务的时间是上一次任务的开始时间加上 delay 时间。

###### 4.测试 scheduleAtFixedRate 方法任务延时
　　如果执行任务的时间被延时（执行任务的时间大于 delay 的时间），那么下一次任务的执行时间以上一次任务“结束”时的时间为参考来计算。

###### 5.验证 schedule 方法不具有追赶执行性
　　任务计划时间早于当前时间，而任务计划时间到当前时间之间的时间所对应的 Task 任务被取消了，不执行了。这就是 Task 任务不追赶的情况。

###### 6. 验证 scheduleAtFixedRate 方法具有追赶执行性
　　任务计划时间早于当前时间，而任务计划时间到当前时间之间的时间所对应的 Task 任务被“补充性”执行了，这就是 Task 任务追赶执行的特性。

## 5.2 本章总结
　　通过本章的学习，应该掌握如何在 Java 中使用定时任务的功能，并且可以对这些定时任务使用指定的 API 进行处理。



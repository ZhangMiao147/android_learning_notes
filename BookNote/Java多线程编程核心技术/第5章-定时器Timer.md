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

　　凡是使用方法中带有 period 参数的，

#### 5.1.5 方法 scheduleAtFixedRate(TimerTask task,Date firstTime,long period) 的测试
　　

###### 1.测试 schedule 方法任务不延时
　　

###### 2.测试 schedule 方法任务延时
　　

###### 3.测试 scheduleAtFixedRate 方法任务不延时
　　

###### 4.测试 scheduleAtFixedRate 方法任务延时
　　

###### 5.验证 schedule 方法不具有追赶执行性
　　


###### 6. 验证 scheduleAtFixedRate 方法具有追赶执行性
　　


## 5.2 本章总结
　　



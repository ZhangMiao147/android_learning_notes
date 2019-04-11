# 第十二章 Android 特色开发，使用传感器
1. 手机内置的传感器是一种微型的物理设备，它能够探测、感受到外界的信号，并按一定规律转换成我们所需要的信息。 Android 手机通常都会支持多种类型的传感器，如光照传感器、加速度传感器、地磁传感器、压力传感器、温度传感器等。

2. 光照传感器在 Android 中的应用还是比较常见的，比如系统就有个自动调整屏幕亮度的功能。它会检测手机周围环境的光照强度，然后对手机屏幕的亮度进行相应地调整，以此保证不管在强光还是弱光下，手机屏幕都能够看得清。

3. Android 中每个传感器的用法其实都比较类似。首先第一步要获取到 SensorManager 的实例，方法如下：
```
         SensorManager sensorManager = (SensorManager)getSystemService(Context.SENSOR_SERVICE);
```
　　SensorManager 是系统所有传感器的管理器，有了它的实例之后就可以调用 getDefaultSensor() 方法来得到任意的传感器类型了，如下所示：
```
	      Sensor sensor = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);
```
　　这里使用 Sensor.TYPE_LIGHT 常量来指定传感器类型，此时的 Sensor 实例就代表着一个光照传感器。
　　接下来需要对传感器输出的信号进行监听，这就要借助 SensorEventListener 来实现了。SensorEventListener 是一个接口，其中定义了 onSensorChanged() 和 onAccuracyChanged() 这两个方法。当传感器的精度发生变化时就会调用 onAccuracyChanged() 方法，当传感器监测到的数值发生变化时就会调用 onSensorChanged() 方法。 onSensorChanged() 方法中传入了一个 SensorEvent 参数，这个参数里又包含了一个 values 数组，所有传感器输出的信息都是存放在这里的。
　　下来还需要调用 SensorManager 的 registerListener() 方法来注册 SensorEventListener 才能使其生效，registerListener()方法来注册SensorEventListener才能使其生效，registerListener()方法接收三个参数，第一个参数就是SensorEventListener的实例，第二个参数是Sensor的实例。第三个参数是用于表示传感器输出信息的更新速率，共有SENSOR_DELAY_UI、SENSOR_DELAY_NORMAL、SENSOR_DELAY_GAME和SENSOR_DELAY_FASTEST这四个值可选，它们的更新速率是一次递增的。
　　始终要记得，当程序退出或传感器使用完毕时，一定要调用 unregisterListener() 方法将使用的资源释放掉。

4. 获取 Sensor 实例的时候要指定一个加速度传感器的常量，如下所示：
```
         Sensor sensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
```
　　加速度传感器输出的信息同样也是存放在 SensorEvent 的 values 数组中的，只不过此时的 values 数组中会有三个值，分别代表手机在 X 轴、Y 轴、Z 轴方向上的加速度。

5. 获取到一个用于表示方向传感器的 Sensor 实例，如下所示：
```
         Sensor sensor = sensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION);
```
　　之后在 onSensorChanged() 方法中通过 SensorEvent 的 values 数组，就可以得到传唤器输出的所有值了。方向传感器会记录手机在所有方向上的旋转角度。其中，values[0] 记录着手机围绕 Z 轴的旋转角度，values[1] 记录着手机围绕 X 轴的旋转角度，values[2] 记录着手机围绕 Y 轴的旋转角度。
　　但遗憾的是，Android 早就废弃了 Sensor.TYPE_ORIENTATION 这种传感器类型，虽然代码还是有效的，但已经不再推荐这么写了。事实上，Android 获取手机旋转的方向和角度是通过加速度传感器和地磁传感器共同计算得出的，这也是Android 目前推荐使用的方式。
　　首先我们需要分别获取到加速度传感器和地磁传感器的实例，并给它们注册监听器，如下所示：
```
	      Sensor accelerometerSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
	      Sensor magneticSensor = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
	      sensorManager.registerListener(listener, accelerometerSensor,SensorManager.SENSOR_DELAY_GAME);
              sensorManager.registerListener(listener, magneticSensor,SensorManager.SENSOR_DELAY_GAME);
```
　　由于方向传感器的精确度要求通常都比较高，这里我们把传感器输出此案次的更新速率提高了一些，使用的是 SENSOR_DELAY_GAME。
　　接下来在 onSensorChanged() 方法中可以获取到 SensorEvent 的 values 数组，分别记录着加速度传感器和地磁传感器输出的值。然后将这两个值传入到 SensorManager 的 getRotationMatrix() 方法中就可以得到一个包含旋转矩阵的R数组，如下所示：
```
	      SensorManager.getRotationMatrix(R, null, acceler0meterValues, magneticValues);
```
　　其中第一个参数 R 是一个长度为 9 的 float 数组，getRotationMatrix() 方法计算出的旋转数组就会赋值到这个数组中。第二个参数是一个用于将地磁向量转换成重力坐标的旋转矩阵，通常指定为 null 即可。第三和第四个参数则分别就是加速度传感器和地磁传感器输出的 values 值。
　　得到了 R 数组之后，接着就可以调用 SensorManager 的 getOrientation() 方法来计算手机的旋转数据了，如下所示：
```
	      SensorManager.getOrientation(R, values);
```
　　values 是一个长度为 3 的 float 数组，手机在各个方向上的旋转数据都会被存放到这个数组当中。其中 values[0] 记录着手机围绕 Z 轴的旋转弧度，values[1] 记录着手机围绕 X 轴的旋转弧度，values[2] 记录着手机围绕 Y 轴的旋转弧度。
　　注意这里计算出的数据都是以弧度为单位的，因此如果你想将它们转换成角度还需要调用如下方法：
```
	      Math.toDegrees(values[0]);
```


# 第十二章 Android 特色开发，使用传感器
    1.手机内置的传感器是一种微型的物理设备，它呢能够探测、感受到外界的信号，并按一定规律转换成我们所需要的信息。Android手机通常都会支持多种类型的传感器，如光照传感器、加速度传感器、地磁传感器、压力传感器、温蒂传感器等。
    2.光照传感器在Android中的应用还是比较常见的，比如系统就有个自动调整屏幕亮度的功能。它会检测手机周围环境的光照强度，然后对手机屏幕的亮度进行相应地调整，一次保证不管在强光还是弱光下，手机屏幕都能够看得清。
    3.Android中每个传感器的用法其实都比较类似。首先第一步要获取到SensorManager的实例，方法如下：
         SensorManager sensorManager = (SensorManager)getSystemService(Context.SENSOR_SERVICE);
	 SensorManager是系统所有传感器的管理器，有了它的实例之后就可以调用getDefaultSensor()方法来得到任意的传感器类型了，如下所示：
	      Sensor sensor = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);
	 这里使用Sensor.TYPE_LIGHT常量来指定传感器类型，此时的Sensor实例就代表着一个光照传感器。
	 接下来需要对传感器输出的信号进行监听，这就要借助SensorEventListener来实现了。SensorEventListener是一个接口，其中定义了onSensorChanged()和onAccuracyChanged()这两个方法。当传感器的精度发生变化时就会调用onAccuracyChanged()方法，当传感器监测到的数值发生变化时就会调用onSensorChanged()方法。onSensorChanged()方法中传入了一个SensorEvent参数，这个参数里又包含了一个values数组，所有传感器输出的信息都是存放在这里的。
	 下来还需要调用SensorManager的registerListener()方法来注册SensorEventListener才能使其生效，registerListener()方法来注册SensorEventListener才能使其生效，registerListener()方法接收三个参数，第一个参数就是SensorEventListener的实例，第二个参数是Sensor的实例。第三个参数是用于表示传感器输出信息的更新速率，共有SENSOR_DELAY_UI、SENSOR_DELAY_NORMAL、SENSOR_DELAY_GAME和SENSOR_DELAY_FASTEST这四个值可选，它们的更新速率是一次递增的。
	 始终要记得，当程序退出或传感器使用完毕时，一定要调用unregisterListener()方法将使用的资源释放掉。
    4.获取Sensor实例的时候要指定一个加速度传感器的常量，如下所示：
         Sensor sensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
	 加速度传感器输出的信息同样也是存放在SensorEvent的values数组中的，只不过此时的values数组中会有三个值，分别代表手机在X轴、Y轴、Z轴方向上的加速度。
    5.获取到一个用于表示方向传感器的Sensor实例，如下所示：
         Sensor sensor = sensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION);
	 之后再onSensorChanged()方法中通过SensorEvent的values数组，就可以得到传唤器输出的所有值了。方向传感器会记录手机在所有方向上的旋转角度。其中,values[0]记录着手机围绕Z轴的旋转角度，values[1]记录着手机围绕X轴的旋转角度，values[2]记录着手机围绕Y轴的旋转角度。
	 但遗憾的是，Android早就废弃了Sensor.TYPE_ORIENTATION这种传感器类型，虽然代码还是有效的，但已经不再推荐这么写了。事实上，Android获取手机旋转的方向和角度是通过加速度传感器和地磁传感器共同计算得出的，这也是Android目前推荐使用的方式。
	 首先我们需要分别获取到加速度传感器和地磁传感器的实例，并给它们注册监听器，如下所示：
	      Sensor accelerometerSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
	      Sensor magneticSensor = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
	      sensorManager.registerListener(listener, accelerometerSensor,SensorManager.SENSOR_DELAY_GAME);
              sensorManager.registerListener(listener, magneticSensor,SensorManager.SENSOR_DELAY_GAME);
	 由于方向传感器的精确度要求通常都比较高，这里我们把传感器输出此案次的更新速率提高了一些，使用的是SENSOR_DELAY_GAME。
	 接下来在onSensorChanged()方法中可以获取到SensorEvent的values数组，分别记录着加速度传感器和地磁传感器输出的值。然后将这两个值传入到SensorManager的getRotationMatrix()方法中就可以得到一个包含旋转矩阵的R数组，如下所示：
	      SensorManager.getRotationMatrix(R, null, acceler0meterValues, magneticValues);
	 其中第一个参数R是一个长度为9的float数组，getRotationMatrix()方法计算出的旋转数组就会赋值到这个数组中。第二个参数是一个用于将地磁向量转换成重力坐标的旋转矩阵，通常指定为null即可。第三和第四个参数则分别就是加速度传感器和地磁传感器输出的values值。
	 得到了R数组之后，接着就可以调用SensorManager的getOrientation()方法来计算手机的旋转数据了，如下所示：
	      SensorManager.getOrientation(R, values);
	 values是一个长度为3的float数组，手机在各个方向上的旋转数据都会被存放到这个数组当中。其中values[0]记录着手机围绕Z轴的旋转弧度，values[1]记录着手机围绕X轴的旋转弧度，values[2]记录着手机围绕Y轴的旋转弧度。
	 注意这里计算出的数据都是以弧度为单位的，因此如果你想将它们转换成角度还需要调用如下方法：
	      Math.toDegrees(values[0]);



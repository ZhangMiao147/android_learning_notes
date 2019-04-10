# 第十一章 Android 特色开发，基于位置的服务
    1.基于位置的服务简称LBS，主要的工作原理就是利用无线电通讯网络或GPS等定位方式来确定出移动设备所在的位置。
    2.基于位置的服务在Android中主要借助LocationManager这个类实现。
    3.要想使用LocationManager就必须要先获取到它的实例，我们可以调用Context的getSystemService()方法获取到。getSysytemService()方法接收一个字符串参数用于确定获取系统的哪个服务，这里传入Context.LOCATION_SERVICE即可。因此，获取LocationManager饿实例就可以写成：
         LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
         接着我们需要选择一个位置提供器来确定设备当前的位置。Android中一般有三种位置提供器可供选择，GPS_PROVIDER、NETWORK_PROVIDER和PASSIVE_PROVIDER。其中前两种使用的比较多，分贝表示勇士GPS定位和使用网络定位。这两种定位方式各有特点，GPS定位的精确度比较高，但是非常耗电，而网络定位的精准度较差，但耗电量比较少、我们应该根据自己的实际情况来选择使用哪一种位置提供器，当位置精度要求非常高的时候，最好使用GPS_PROVIDER，二一班情况下，使用NETWORK_PROVIDER会更加得划算。
	 需要注意的是，定位功能必要要由用户主动去启动才行，不然任何应用程序都无法获取到手机当前的位置信息。
	 将选择好的位置提供器传入到getLastKnownLocation()方法中，就可以得到一个Location对象，如下所示：
	      String provider = LocationManager.NETWORK_PROVIDER;
	      Location location = locationManager.getLastKnownLocation(provider);
	 这个Location对象中包含了经度、纬度、海拔等一系列的位置信息，然后从中取出我们所关系的那部分数据即可。
	 判断有哪些位置提供器可用，如下所示：
	      List<String> providerList = locationManager.getProviders(true);
	 getProviders()方法接收一个布尔值参数，传入truw就表示只有启用的位置提供器才会被返回。之后再从providerList中判断是否包含GPS定位的功能就行了。
	 调用getLastKnownLocation()方法虽然可以获取到设备当前的位置信息，但是用户是完全有可能带有移动设备随时移动的，那么怎样才能在设备位置发生改变的时候获取到最新的位置信息呢？LocationManager还提供了一个requestLocationUpdates()方法，只要传入一个LocationListener的实例，并简单配置几个参数就可以实现上述功能了，写法如下：
	      locationManager.requestLocationUpdates(LocationManager.GPS_PRIVIDER, 5000, 10, new LocationListener() {
	           @Override
		   public void onStatusChanged(String provider, int status, Bundle extras){}

		   @Override
		   public void onProviderEnabled(String provider) {}

		   @Override
		   public void onProviderDisabled(String provider) {}

		   @Override
		   public void onLocationChanged(Location location) {}
	      });
         这里requestLocationUpdates()方法接收四个参数，第一个采纳数是位置提供器的类型，第二个参数是监听位置变化的时间间隔，以毫秒为单位，第三个参数是监听位置变化的距离间隔，以米为单位，第四个参数则是LocationListener监听器。
    4.其实Android本身就提供了地理编码的API，主要是使用GeoCoder这个类来实现的。他可以非常简单地完成正向和反向的地理编码功能，从而轻松地将一个经纬值转换成看得懂的位置信息。
         GeoCoder长期存在着一些较为严重的bug，在反向地理编码的时候会有一定的概率不能解析出位置的信息，这样就无法保证位置解析的稳定性。
	 谷歌又提供了一套Geocoding API，使用它的话也可以完成反向地理编码的工作，只不过它的用法稍微复杂了一些，但稳定性要比GeoCoder强得多。
	 Geocoding API的工作原理并不神秘，其实就是利用HTTP协议。在手机端我们可以向谷歌的服务器发起一条HTTP请求，并将经纬度的值作为参数异同传递过去，然后服务端会帮我们将这个经纬值转换成看得懂的位置信息，再将这些信息返回给手机端，最后手机端去解析服务器返回的信息，并进行处理就可以了。
	 Geocoding API中规定了很多借口，其中反向地理编码的接口如下：
	      http://maps.googleapis.com/maps/api/geocode/json?latlng=40.714224,-73.96145&sensor=true_or_false
	 其中http://maps.googleapis.com/maps/api/geocode/是固定的，表示接口的连接地址。json表示希望服务器能够返回JSON格式的数据，这里也可以指定成xml。latlng=40.714224,-73.96145表示传递给服务器去解码的经纬值是北纬40.714224度，西经73.96145度，sensor=true_or_false表示这条请求是否来自于某个设备的位置传感器，通常指定成false即可。
    5.分支是版本控制工具中比较高级且比较重要的一个概念，它主要的作用就是在现有代码的基础上开辟一个分叉口，使得代码可以在主干线和分支线上同时进行开发，且相互之间不会影响。
         查看当前的版本库当中有哪些分支，可以使用git branch -a 命令.
	 创建分支命令： git branch 分支名
         切换分支命令：git checkout 分支名
	 合并分支代码命令：git checkout 分支名（先切换分支） git merge 分支名(合并哪个分支的代码)
	 删除分支命令：git branch -D 分支名
	 将本地修改的内容同步到远程版本库上：git push origin master，其中origin部分指定的是远程版本库的Git地址，master部分指定的是同步哪一个分支上。上述命令就完成了将本地代码同步到版本库的master分支上的功能。
	 将远程版本库上的修改同步到本地。Git提供了两种命令来完成此功能，分别是fetch和pull，fetch的语法规则和push是差不多的，如下所示：
	      git fetch origin master
	      执行命令后，就会将远程版本库上的代码同步到本地，不过同步下来的代码并不会合并到任何分支上，而是会存放在一个origin/master分支上，这是我们可以通过diff命令来查看远程版本库上待敌修改了哪些东西：
	           git diff origin/master
	      之后再调用merge命令将origin/master分支上的修改合并到主分支上即可，如下所示：
	           git merge origin/master
	      而pull命令则是相当于将fetch和merge这两个命令放在一起在执行了，它可以从远程版本库上获取最新的代码并且合并到本地，用法如下：
	           git pull origin master


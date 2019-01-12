# Bitmap too larget to be uploaded into a texture的解决方法

## 问题描述
使用canvas.drawBitmap()系列方法时，抛出错误Bitmap too larget to be uploaded into a texture。

## 问题原因
错误日志的内容是
```
W/OpenGLRenderer: Bitmap too large to be uploaded into a texture (1204x4533, max=4096x4096)
```
所以，可以得知问题的原因是设置src的图片宽高大于了最大接受的值，所以抛出错误。
本来想换着想法实现，经过测试发现设置background，src都会有这样的问题出现。

## 解决方法
使用BitmapFactory.decodeStream()系列方法将图片的宽高进行压缩。
```
	if (bitmap != null) {
    	int maxWidth = canvas.getMaximumBitmapWidth();
    	int maxHeight =  canvas.getMaximumBitmapHeight();
    	int width = bitmap.getWidth();
    	int height = bitmap.getHeight();
    	if (width > maxWidth || height > maxHeight) {
    		int inSampleSize;
        	int heightRatio = Math.round((float) height / (float) maxHeight);
       		int widthRatio = Math.round((float) width / (float) maxWidth);
        	inSampleSize = heightRatio < widthRatio ? widthRatio : heightRatio;
        	if (inSampleSize == 1) {
        		inSampleSize = 2;
        	}
        	ByteArrayOutputStream baos = new ByteArrayOutputStream();
        	bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        	ByteArrayInputStream isBm = new ByteArrayInputStream(baos.toByteArray());
        	BitmapFactory.Options options = new BitmapFactory.Options();
        	options.inSampleSize = inSampleSize;
        	options.inJustDecodeBounds = false;
        	bitmap = BitmapFactory.decodeStream(isBm, null, options);
		}
    }
```
将上面这段代码添加到合适的位置，问题就解决了。

## 参考文章
https://blog.csdn.net/vickywinner/article/details/53164702

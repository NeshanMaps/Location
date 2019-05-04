# NeshanLocation
[![](https://jitpack.io/v/NeshanMaps/Location.svg)](https://jitpack.io/#NeshanMaps/Location)


<div dir=rtl>
  
  این کتابخانه برای پیدا کردن موقعیت مکانی کاربر در [نقشه نشان](https://developers.neshan.org/) برای اندروید 4.2 به بالا قابل استفاده است .

<div align=center>
  
   ![Screenshot](https://github.com/NeshanMaps/NeshanLocation/blob/master/resources/location.png)
  
</div>

دایره ای که پیرامون مکان نشان داده میشود ، میزان دقت مکان دریافتی و احتمال حضور در این محدوده را نشان میدهد .



## طریقه استفاده
ریپوزیتوری JitPack را به فایل build.gradle روت پروژه اضافه کنین:

<div dir=ltr>

  ```gradle
  
   allprojects {
		repositories {
			...
			maven { url 'https://jitpack.io' }
		}
	}
  
  ```
</div>

وابستگی زیر را به وابستگی های فایل `build.gradle` ماژول خود اضافه کنید .

  

<div dir=ltr>

  ```gradle
  
    dependencies {
        ...
   	 implementation 'com.github.NeshanMaps:Location:1.0.0'
      }
  
  ```
</div>

#### اینترفیس `OnLocationEventListener` را implement کنید
با implement کردن این اینترفیس ، برای دریافت نتیجه مکان یابی باید دو متد `onLocationReceived` و `onLocationFailed` را Override کنید . 
  
  <div dir=ltr>
  
  ```java
  
     @Override
    public void onLocationReceived(Location location) {
        Log.i(TAG, "onLocationReceived: "+location.getLongitude(), location.getLatitude());
    }

    @Override
    public void onLocationFailed(LocationFailed failed) {
        if (failed == LocationFailed.NO_LOCATION) {
            Log.i(TAG, "onLocationFailed: location off");
        } else {
            Log.i(TAG, "onLocationFailed: no permisiion");
        }

    }
  
  ```
  
  </div>
  
  #### یک نمونه از کلاس `NeshanLocation` بسازید
سازنده این کلاس به `MapView` و `Context` نیاز دارد .
<div dir=ltr>
  
  ```java
   NeshanLocation neshanLocation = new NeshanLocation(this, map);
  ```
  
  </div>
  
  #### دریافت خودکار مکان
   برای دریافت متناوب مکان ، از متد زیر استفاده کنید و زمان مورد نظرتان را به میلی ثانیه به آن بدهید .
    <div dir=ltr>
  
  ```java
  
     neshanLocation.startAutoLocationUpdate(6000);

  ```
  
  </div>

 
 #### دریافت مکان فعلی
 برای دریافت مکان بدون آپدیت خودکار ، از متد زیر استفاده کنید .
 
 <div dir=ltr>
  
  ```java
     neshanLocation.getCurrentLocation();

  ```
  
  </div>
  
  #### توقف دریافت مکان
  بهتر است در متد onPause اکتیویتی از این متد استفاده کنید تا بعد از خروج از اکتیویتی ، دریافت مکان متوقف شود .
  
   <div dir=ltr>
  
  ```java
     neshanLocation.stopAutoLocationUpdate();

  ```
  
  </div>
  

  <div align=center>
  
   ![Screenshot](https://github.com/NeshanMaps/Location/blob/master/resources/location.gif)
  
</div>
   
   ## شخصی سازی ظاهر نمایش مکان
  
  <div dir=ltr>
  
  ```java

     void setMarkerIcon(int markerIcon)
     void setMarkerSize(float markerSize)
     void setCircleFillColor(int color)
     void setCircleStrokeColor(int color)
     void setCircleOpacity(float opacity)
     void setCircleStrokeWidth(float width)
     void setCircleVisible(boolean visible)
     void setMarkerMode(MarkerOrientation markerMode)
     void setRippleEnable(boolean enable)

  ```
  
  </div>
 
  
  خطوط زیر ، مارکر نمایش مکان و رنگ دایره پیرامون آن را تغییر میدهد .
  
  <div dir=ltr>
  
  ```java
     neshanLocation.setMarkerIcon(R.drawable.your_ic_marker);
     neshanLocation.setCircleFillColor(ContextCompat.getColor(this , R.color.your_color));
     neshanLocation.setCircleStrokeColor(ContextCompat.getColor(this , R.color.your_color));

  ```
  
  </div>
  
 
  
</div>

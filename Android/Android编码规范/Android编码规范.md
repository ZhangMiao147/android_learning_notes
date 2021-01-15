# Android 编码规范

1. 资源文件需带模块前缀。

   ```
   ai_aiassistant_highlight_bg
   ```

   ai_aiassistant 就是模块名。

2. layout 文件的命名方式。

   ```
   Activity 的 layout 以 model_activity 开头
   Fragment 的 layout 以 model_fragment 开头
   Dialog 的 layout 以 module_dialog 开头
   include 的 layout 以 module_include 开头
   ListView 的 layout 以 module_list_item 开头
   RecyclerView 的 item layout 以 module_recycle_item 开头
   GridView 的行 layout 以 module_gird_item 开头
   ```



3. drawable 资源名称以小写单词 + 下划线的方式命名，根据分辨率不同存放在不同的 drawable 目录下，建议只使用一套，例如 drawable-xhdpi。采用规则如下：模块名\_业务功能描述\_空间状态限定词

   ```
   module_login_btn_pressed
   module_tabs_icon_home_normal
   ```

   

4. anim 资源名称以小写单词 + 下划线的方式命名，采用以下规则：模块名\_逻辑名称\_[方向|序号]

   * Tween 动画资源（使用简单图像变换的动画，例如缩放、平移）：尽可能以通用的动画名称命名，动画 + 方向

     ```
     module_fade_in、module_fade_out、module_push_down_in
     ```

   * Frame 动画资源（按帧顺序播放图像的动画）：尽可能以模块 + 功能命名 + 序号。

     ```
     module_loading_grey_001
     ```

5. Color 资源使用 # AARRGGBB 格式，写入 module_colors.xml 文件中，命名格式采用一下规则：

   模块名_逻辑名称\_颜色

   ```xml
   <color name = "module_btn_bg_color" >#33b5e5e5</color>
   ```

6. dimen 资源以小写单词 + 下划线方式命名，写入 module_dimens.xml 文件中，采用以下规则：

   模块名_描述信息

   ```xml
   <dimen name="module_horizontal_line_height">1dp</dimen>
   ```

7. style 资源采用 " 父 style 名称，当前 style 名称 " 方式命名，写入 module_styles.xml 文件中，首字母大写。

   ```xml
   <style name="ParentTheme.TheActivityTheme">
   	...
   </style>
   ```

8. string 资源文件或者文本用到字符需要全部写入 module_strings.xml 文件中，字符串以小写单词 + 下划线的方式命名，采用以下规则：

   模块名_逻辑名称

   ```
   module_login_tips
   module_homepage_notice_desc
   ```

9. id 资源原则上以驼峰法命名，View 组建的资源 id 建议以 View 的缩写作为前缀。常用缩写表如下：

   | 控件             | 缩写 |
   | ---------------- | ---- |
   | LinearLayout     | ll   |
   | RelativeLayout   | rl   |
   | ConstraintLayout | cl   |
   | ListView         | lv   |
   | ScrollView       | sv   |
   | TextView         | tv   |
   | Button           | btn  |
   | ImageView        | iv   |
   | CheckBox         | cb   |
   | RadioButton      | rb   |
   | EditText         | et   |

   其他控件的缩写推荐使用小写字母并用下划线进行分割，例如：ProgressBar 对应的缩写为 progress_bar；DatePicker 对应的缩写为 date_picker。

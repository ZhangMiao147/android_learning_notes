# uses-permission 和 permission 区别及使用

## uses-permission 和 permission 的区别

* permission 定义权限
* uses-permission 申请权限

## uses-permission(权限申请)

### 介绍

Android 应用**必须请求访问敏感用户数据**（例如联系人和短信）或某些系统功能（例如相机和互联网访问）的权限。**每个权限都由一个唯一的标签标识**。例如，需要发送 SMS 消息和访问互联网的应用程序必须在清单中包含以下内容：

```xml
      <manifest ... >
          <uses-permission android:name="android.permission.SEND_SMS"/>
          <uses-permission android:name="android.permission.INTERNET" />
          ...
      </manifest>
```

 **添加自定义权限，如：**

```xml
      <manifest ... >
          <uses-permission android:name="com.scc.userprovider.permissionread"/>
      <uses-permission android:name="com.scc.userprovider.permissionwrite"/>
          ...
      </manifest>
```

权限**不仅用于请求系统功能**。你还可以**限制其他应用与你的应用组件交互的方式**。

## permission(自定义权限)

### 介绍

你的应用可以使用声明安全权限，可用于限制对此应用或其他应用的特定组件或功能的访问，例如 ContentProvider。

```xml
      <permission
          android:description="string resource"
          android:icon="drawable resource"
          android:label="string resource"
          android:name="string"
          android:permissionGroup="string"
          android:protectionLevel=["normal" | "dangerous" |
                                 "signature" | ...] />
```

* **android:description**：权限说明。此属性必须设置为队字符串资源的引用。

- **android:icon**：权限的图标。
- **android:label**：向用户显示的权限的名称。可将此标签直接设置为原始字符串。不过，当准备好发布应用时，应将标签设置为对字符串资源的引用，以便可以像界面中的其他字符串一样进行本地化。
- **android:name**：用于引用权限的名称。（例如，在元素和应用组件的 permission 属性中）
- **android:permissionGroup**：将此权限分配给一个组。如果未设置此属性，则此权限不会属于某个组。
- **android:protectionLevel**： 说明权限中隐含的潜在风险，并指示系统在确定是否将权限授予请求授权的应用时应遵循的流程。下表列出了所有基本权限类型。
  - **normal**：默认值。具有较低风险的权限。系统会自动向在安装时请求授权的应用授予此类权限，**无需征得用户的明确许可**（但用户始终可以选择在安装之前查看这些权限）。
  - **dangerous**：具有较高风险的权限。由于此类权限会带来潜在风险，因此系统可能**不会自动向请求授权的应用授予此类权限**。
  - **signature** ：只有在请求授权的应用使用与声明权限的应用相同的证书进行签名时系统才会授予的权限。**如果证书匹配**，则系统会在不通知用户或征得用户明确许可的情况下自动授予权限。
  - **signatureOrSystem**：**不要使用此选项，因为 signature 保护级别应足以满足大多数需求**，无论应用安装在何处，该保护级别都能正常发挥作用。signatureOrSystem 权限适用于以下特殊情况：多个供应商将应用内置到一个系统映像中，并且需要明确共享特定功能，因为这些功能是一起构建的。(除了满足signature的应用可以申请外，存放在系统目录 `/system/app` 目录下也可以申请。)

### permission 样例

应用 Demo(com.scc.cp)和其他应用(com.scc.ha)。

#### 1. 先使用定义一个权限

```xml
          <permission android:description="@string/permission_description"
              android:icon="@mipmap/ic_launcher"
              android:label="permissionLabel"
              android:name="com.scc.userprovider.permission"
              android:protectionLevel="normal"/>
```

#### 2. provider 组件设置权限

```xml
          <provider
              android:authorities="com.scc.userprovider"
              android:name="com.scc.cp.UserProvider"
              android:permission="com.scc.userprovider.permission"
              android:exported="true"/>
```

#### 3. 其他应用（com.scc.ha）使用 com.scc.cp 包加权限的 UserProvider

```xml
<uses-permission android:name="com.scc.userprovider.permission"/>
```

然后就可以使用 com.scc.cp 包中的 provider 数据了。

## permission-group（自定义权限组）

### 介绍

```xml
      <permission-group
          android:description="string resource"
          android:icon="drawable resource"
          android:label="string resource"
          android:name="string" />
```

声明相关权限的逻辑分组的名称。各个权限通过元素的 permissionGroup 属性加入权限组中。权限组中的成员一起显示在界面中。

 **注意**：此元素并不声明权限本身，而只声明可以放置权限的类别。

 permission-group 属性介绍跟 permission 类似。

### permission-group 样例

应用 Demo(com.scc.cp)和其他应用(com.scc.ha)。

#### 1. 先使用定义一个权限组

```xml
          <permission-group
              android:name="com.scc.userprovider.permissiongroup"
              android:description="@string/userprovider_permission_group_description"
              android:icon="@mipmap/ic_launcher"
              android:label="GroupLabel"/>
```

#### 2. 添加组员

```xml
          <permission
              android:name="com.scc.userprovider.permissionread"
              android:description="@string/userprovider_permission_read_description"
              android:icon="@mipmap/ic_launcher"
              android:label="readLabel"
              android:permissionGroup="com.scc.userprovider.permissiongroup"
              android:protectionLevel="normal"/>
          <permission
              android:name="com.scc.userprovider.permissionwrite"
              android:description="@string/userprovider_permission_write_description"
              android:icon="@mipmap/ic_launcher"
              android:label="writeLabel"
              android:permissionGroup="com.scc.userprovider.permissiongroup"
              android:protectionLevel="normal"/>
```

#### 3. provider 组件设置权限

```xml
        <provider
          android:authorities="com.scc.userprovider"
          android:name="com.scc.cp.UserProvider"
          android:writePermission="com.scc.userprovider.permissionwrite"
          android:readPermission="com.scc.userprovider.permissionread"
          android:exported="true"/>
```

#### 4. 其他应用（com.scc.ha）使用 com.scc.cp 包加权限的 UserProvider

申请权限：

```xml
      <uses-permission android:name="com.scc.userprovider.permissionread"/>
      <uses-permission android:name="com.scc.userprovider.permissionwrite"/>
```

然后就可以使用 com.scc.cp 包中的 provider 数据了。

## 参考文章

1. [uses-permission和permission区别及使用](https://bbs.huaweicloud.com/blogs/318259)
2. [权限标签 permission 和 uses-permission](https://www.jianshu.com/p/1f8ecb6c3285)
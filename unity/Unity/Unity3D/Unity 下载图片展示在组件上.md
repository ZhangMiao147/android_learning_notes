## 1. 下载库
```csharp
using System;
using System.Collections;
using System.Collections.Generic;
using UnityEngine;
// https://blog.csdn.net/qq_34691688/article/details/84976290
public class DownloadWWW : MonoBehaviour
{
    private static DownloadWWW instance;
    public static DownloadWWW Instance {
        get
        {
            if (instance == null) {
                GameObject obj = new GameObject();
                instance = obj.AddComponent<DownloadWWW>();
            }
            return instance;
        }
    }

    private void Awake()
    {
        DontDestroyOnLoad(gameObject);
    }
    /**
     * 下载 url 资源的文本内容
     * @params url url 资源
     * @params isAddRandomParams 是否添加随机参数
     * @params callback 下载完毕回调函数
     * @params retryCount 本次下载失败后的重试次数
     */
    public void DownloadText(string url, bool isAddRandomParams, Action<bool,string> callback, Action<float> progress, int retryCount) {
        if (isAddRandomParams) {
            url = GetRandomParamsUrl(url);
        }
        Debug.Log("URL:"+url);
        StartCoroutine(DoDownloadText(url, callback, progress, retryCount));
    }

    private IEnumerator DoDownloadText(string url, Action<bool, string> callback, Action<float> progress, int retryCount) {
        var www = new WWW(url);
        while (!www.isDone) {
            if (progress != null) {
                progress(www.progress);
            }
            yield return null;
        }
        if (progress != null) {
            progress(www.progress);
        }
        if (string.IsNullOrEmpty(www.error))
        {
            if (callback != null)
            {
                callback(true, www.text);
            }
        }
        else {
            if (retryCount <= 0)
            {
                // 彻底失败
                Debug.LogError(string.Concat("DownloadText Failed！URL:", url, ",Error:", www.error));
                if (callback != null)
                {
                    callback(false, null);
                }
            }
            else {
                // 继续尝试
                yield return StartCoroutine(DoDownloadText(url,callback,progress,--retryCount));
            }
        }
    }

    /**
     * 下载 url 资源的字节内容
     */
    public void DownloadBytes(string url, bool isAddRandomParams, Action<bool, byte[]> callback, Action<float> progress, int retryCount) {
        if (isAddRandomParams) {
            url = GetRandomParamsUrl(url);
        }
        Debug.Log("URL:"+url);
        StartCoroutine(DoDownloadBytes(url,callback,progress,retryCount));
    }

    private IEnumerator DoDownloadBytes(string url, Action<bool, byte[]> callback, Action<float> progress, int retryCount) {
        var www = new WWW(url);
        while (!www.isDone) {
            if (progress != null) {
                progress(www.progress);
                yield return null;
            }
        }
        if (progress != null) {
            progress(www.progress);
        }
        if (string.IsNullOrEmpty(www.error))
        {
            if (callback != null)
            {
                callback(true, www.bytes);
            }
        }
        else {
            if (retryCount <= 0)
            {
                // 彻底失败
                Debug.LogError(string.Concat("DownloadBytes Failed! URL：", url, ",Error：" + www.error));
                if (callback != null)
                {
                    callback(false, null);
                }
            }
            else {
                // 继续尝试
                yield return StartCoroutine(DoDownloadBytes(url,callback,progress,--retryCount));
            }
        }
    }

    /**
     * 给原始 url 加上随机参数
     * */
    private string GetRandomParamsUrl(string url) {
        var r = new System.Random();
        var u = string.Format("{0}?type={1}&ver={2}&sign={3}",url.Trim(),r.Next(100),r.Next(100),Guid.NewGuid().ToString().Substring(0,8));
        return u;
    }

    public void GetHttp(string url, Action<string> onDone, Action<string> onFail) {
        Debug.Log("URL:"+url);
        StartCoroutine(DoGetHttp(url,onDone,onFail));
    }

    private IEnumerator DoGetHttp(string url, Action<string> onDone, Action<string> onFail) {
        WWW www = new WWW(url);
        yield return www;
        if (string.IsNullOrEmpty(www.error))
        {
            onDone(www.text);
        }
        else {
            Debug.Log("HttpResponse onFail:"+www.error);
            onFail(www.error);
        }
    }

    // Start is called before the first frame update
    void Start()
    {
        
    }

    // Update is called once per frame
    void Update()
    {
        
    }
}

```
## 2. 下载管理库
```csharp
using System.Collections;
using System.Collections.Generic;
using UnityEngine;
using System.Net;
using System.IO;
using System;
using System.Text;
using UnityEngine.UI;
using System.Security.Cryptography;

public class HttpManager : MonoBehaviour
{

    private static HttpManager instance;
    public static HttpManager Instance {
        get {
            if (instance == null) {
                GameObject obj = new GameObject();
                obj.name = "HttpManager";
                instance = obj.AddComponent<HttpManager>();
            }
            return instance;
        }
    }

    /**
     * web 下载物体
     */
    private GameObject webDownloaderObj;

    /**
     * 下载类
     */
    private DownloadWWW downloadWWW;

    /**
     * 缓存词典
     */
    private Dictionary<string, Texture2D> dicHeadTextureMd5;

    /**
     * 下载的图像 list
     */
    private List<string> listHeadName; // 预备下载图像清单

    public void Init() {
        webDownloaderObj = new GameObject("WebDownloader");
        downloadWWW = webDownloaderObj.AddComponent<DownloadWWW>();
        dicHeadTextureMd5 = new Dictionary<string, Texture2D>();
    }

    /**
     * 手机内存读取图片
     */
    public Texture2D LoadTexture2DInStorage(string url) {
        // 创建文件读取流
        FileStream fileStream = new FileStream(url, FileMode.Open, FileAccess.Read);
        fileStream.Seek(0, SeekOrigin.Begin);
        // 创建文件长度缓冲区
        byte[] bytes = new byte[fileStream.Length];
        // 读取文件
        fileStream.Read(bytes, 0, (int)fileStream.Length);
        // 释放文件读取流
        fileStream.Close();
        fileStream.Dispose();
        fileStream = null;

        // 创建 Texture
        int width = 50;
        int height = 50;
        Texture2D texture = new Texture2D(width, height, TextureFormat.RGB24, false);
        texture.LoadImage(bytes);
        return texture;
    }

    /**
     * 字典初始化
     */
    private void InitTempTexture() {
        if (dicHeadTextureMd5 == null) {
            dicHeadTextureMd5 = new Dictionary<string, Texture2D>();
        }
    }

    /**
     * 清单初始化
     */
    private void InitTempLisName()
    {
        if (listHeadName == null) {
            listHeadName = new List<string>();
        }
    }

    /**
     * 检测字典是否包含该图片
     */
    private bool IshaveTempTexture(string textureName) {
        return (dicHeadTextureMd5.ContainsKey(textureName));
    }

    /**
     * 临时存储图片到字典
     */
    public void SaveTempDicTexture(string textureName, Texture2D texture2D) {
        if (!dicHeadTextureMd5.ContainsKey(textureName)) {
            dicHeadTextureMd5.Add(textureName, texture2D);
        }
    }

    /**
     * 从字典读取图片
     */
    public Texture2D LoadTempDicTextture(string textureName) {
        return dicHeadTextureMd5[textureName];
    }

    public Texture2D LoadDefaultTexture(string texNameMd5) {
        Texture2D defTex = Resources.Load<Texture2D>("Face/headdefault");//载入默认头像
        SaveTempDicTexture(texNameMd5, defTex); // 存入字典（临时存储）
        return defTex;
    }

    public void LoadImage(string url, GameObject path) {
        RawImage rawImage = path.GetComponent<RawImage>();
        Renderer renderer = path.GetComponent<Renderer>();
        Debug.Log("LoadImage" + rawImage);
        if (rawImage != null) {
            StartCoroutine(LoadHead(url, rawImage));
        }
        if (renderer != null) {
            StartCoroutine(LoadHead(url, renderer));
        }
    }

    public IEnumerator LoadHead(string url, RawImage logo) {
        int tryCount = 0;
        while (tryCount < 10) // 下载尝试次数
        {
            bool andios = (Application.platform == RuntimePlatform.Android || Application.platform == RuntimePlatform.IPhonePlayer);
            string texName = url;// 图片名称.Remove(0,url.LastIndexof("/")+1)
            string texNameMd5 = UseMd5(texName);// 图片名称加密 md5

            string directoryPath = ""; // 图片文件夹路径
            string directoryMd5Path = ""; // 图片文件完整路径
            bool isLoad = false; // 是否开始下载，否则读内存
            InitTempTexture();
            InitTempLisName(); //初始化清单
            if (!listHeadName.Contains(texNameMd5)) {
                listHeadName.Add(texNameMd5);
                isLoad = true; // 不存在则添加入清单，并进入下载模式
            }
            yield return 0;//下一帧后检测全局
            if (!isLoad)
            {
                float waitTime = 0;
                while (!IshaveTempTexture(texNameMd5) && waitTime < 1.5f)
                { // 字典是否包含该图片，等待快出现的内存
                    waitTime += Time.deltaTime;
                    yield return 0;
                }
                if (waitTime < 1.5f)
                {
                    if (logo)
                    {
                        logo.texture = LoadTempDicTextture(texNameMd5); // 显示 // 有则直接内存
                    }
                }
                else
                {
                    logo.texture = LoadDefaultTexture(texNameMd5);
                }
                tryCount = 10;
                break; // 结束
            }
            else {
                // 无则开始图片加载过程
                if (andios) {
                    // 手机无则创建文件夹
                    // directoryPath = "D:/"+"/headimage";
                    directoryPath = Application.persistentDataPath + "/headimage";// 文件夹路径
                    Debug.Log("<color=yellow>图片保存路径：：：" + directoryPath + "</color>");
                    if (!Directory.Exists(directoryPath)) {
                        Directory.CreateDirectory(directoryPath); // 无则创建文件夹
                    }
                    while (!Directory.Exists(directoryPath)) {
                        yield return 0;// 创建时间，检测文件夹是否存在
                    }
                    directoryMd5Path = directoryPath + "/" + texNameMd5; // 图片文件完整路径

                    // 读取手机内存中文件夹里的图片
                    if (File.Exists(directoryMd5Path)) {
                        Debug.Log("导入手机内存图片");
                        Texture2D newTex = LoadTexture2DInStorage(directoryMd5Path); //读取
                        logo.texture = newTex; // 显示
                        Debug.Log("<color = #FFD505> ++++++++      " + newTex.name + "++++++++++++++++++++++ </color> ");
                        SaveTempDicTexture(texNameMd5, newTex);//存入字典（临时存储）
                        tryCount = 10;
                        break; //结束
                    }
                }
                if (!andios || (andios && !File.Exists(directoryMd5Path)))
                {
                    // 从服务器下载图片后存入内存
                    if (url.Length <= 10)
                    {
                        // 长度不够则为无效 url，载入默认头像
                        logo.texture = LoadDefaultTexture(texNameMd5);
                        tryCount = 10;
                        break; // 结束
                    }
                    else {
                        // 长度够则为有效 url，下载头像
                        WWW www = new WWW(url);
                        yield return www;
                        Debug.Log("头像下载完毕");

                        if (string.IsNullOrEmpty(www.error))
                        {
                            // www 缓存时有缺陷，2 种方式存内存
                            Texture2D wwwTex = new Texture2D(www.texture.width, www.texture.height, TextureFormat.RGB24, false);
                            www.LoadImageIntoTexture(wwwTex);
                            logo.texture = wwwTex;//显示
                            SaveTempDicTexture(texNameMd5, wwwTex); // 存入字典（临时存储）
                            if (andios && !File.Exists(directoryMd5Path))
                            {
                                WebTestDownload(url, directoryMd5Path); //存入手机内存
                            }
                            while (!File.Exists(directoryMd5Path))
                            {
                                yield return 0;
                            }
                            Texture2D newTex = LoadTexture2DInStorage(directoryMd5Path); //读取内置
                            SaveTempDicTexture(texNameMd5, newTex); //存入字典（临时存储）
                            tryCount = 10;
                            break; //结束
                        }
                        else {
                            tryCount++; //重新下载尝试+1
                        }
                    }

                }
            }
        }

    }

    public IEnumerator LoadHead(string url, Renderer renderer)
    {
        int tryCount = 0;
        while (tryCount < 10) // 下载尝试次数
        {
            bool andios = (Application.platform == RuntimePlatform.Android || Application.platform == RuntimePlatform.IPhonePlayer);
            string texName = url;// 图片名称.Remove(0,url.LastIndexof("/")+1)
            string texNameMd5 = UseMd5(texName);// 图片名称加密 md5

            string directoryPath = ""; // 图片文件夹路径
            string directoryMd5Path = ""; // 图片文件完整路径
            bool isLoad = false; // 是否开始下载，否则读内存
            InitTempTexture();
            InitTempLisName(); //初始化清单
            if (!listHeadName.Contains(texNameMd5))
            {
                listHeadName.Add(texNameMd5);
                isLoad = true; // 不存在则添加入清单，并进入下载模式
            }
            yield return 0;//下一帧后检测全局
            if (!isLoad)
            {
                float waitTime = 0;
                while (!IshaveTempTexture(texNameMd5) && waitTime < 1.5f)
                { // 字典是否包含该图片，等待快出现的内存
                    waitTime += Time.deltaTime;
                    yield return 0;
                }
                if (waitTime < 1.5f)
                {
                    if (renderer)
                    {
                        renderer.material.mainTexture = LoadTempDicTextture(texNameMd5); // 显示 // 有则直接内存
                    }
                }
                else
                {
                    renderer.material.mainTexture = LoadDefaultTexture(texNameMd5);
                }
                tryCount = 10;
                break; // 结束
            }
            else
            {
                // 无则开始图片加载过程
                if (andios)
                {
                    // 手机无则创建文件夹
                    // directoryPath = "D:/"+"/headimage";
                    directoryPath = Application.persistentDataPath + "/headimage";// 文件夹路径
                    Debug.Log("<color=yellow>图片保存路径：：：" + directoryPath + "</color>");
                    if (!Directory.Exists(directoryPath))
                    {
                        Directory.CreateDirectory(directoryPath); // 无则创建文件夹
                    }
                    while (!Directory.Exists(directoryPath))
                    {
                        yield return 0;// 创建时间，检测文件夹是否存在
                    }
                    directoryMd5Path = directoryPath + "/" + texNameMd5; // 图片文件完整路径

                    // 读取手机内存中文件夹里的图片
                    if (File.Exists(directoryMd5Path))
                    {
                        Debug.Log("导入手机内存图片");
                        Texture2D newTex = LoadTexture2DInStorage(directoryMd5Path); //读取
                        renderer.material.mainTexture = newTex; // 显示
                        Debug.Log("<color = #FFD505> ++++++++      " + newTex.name + "++++++++++++++++++++++ </color> ");
                        SaveTempDicTexture(texNameMd5, newTex);//存入字典（临时存储）
                        tryCount = 10;
                        break; //结束
                    }
                }
                if (!andios || (andios && !File.Exists(directoryMd5Path)))
                {
                    // 从服务器下载图片后存入内存
                    if (url.Length <= 10)
                    {
                        // 长度不够则为无效 url，载入默认头像
                        renderer.material.mainTexture = LoadDefaultTexture(texNameMd5);
                        tryCount = 10;
                        break; // 结束
                    }
                    else
                    {
                        // 长度够则为有效 url，下载头像
                        WWW www = new WWW(url);
                        yield return www;
                        Debug.Log("头像下载完毕");

                        if (string.IsNullOrEmpty(www.error))
                        {
                            // www 缓存时有缺陷，2 种方式存内存
                            Texture2D wwwTex = new Texture2D(www.texture.width, www.texture.height, TextureFormat.RGB24, false);
                            www.LoadImageIntoTexture(wwwTex);
                            renderer.material.mainTexture = wwwTex;//显示
                            SaveTempDicTexture(texNameMd5, wwwTex); // 存入字典（临时存储）
                            if (andios && !File.Exists(directoryMd5Path))
                            {
                                WebTestDownload(url, directoryMd5Path); //存入手机内存
                            }
                            while (!File.Exists(directoryMd5Path))
                            {
                                yield return 0;
                            }
                            Texture2D newTex = LoadTexture2DInStorage(directoryMd5Path); //读取内置
                            SaveTempDicTexture(texNameMd5, newTex); //存入字典（临时存储）
                            tryCount = 10;
                            break; //结束
                        }
                        else
                        {
                            tryCount++; //重新下载尝试+1
                        }
                    }

                }
            }
        }

    }

    //32位md5加密
    public static string UseMd5(string str)
    {
        //md5对象
        MD5 md5 = new MD5CryptoServiceProvider();

        //按照utf8将字符串转换为二进制,再进行md5加密
        byte[] strResult = md5.ComputeHash(Encoding.UTF8.GetBytes(str));

        //BitConverter转换出来的字符串会在每个字符中间产生一个分隔符，需要去除掉
        string sResult = BitConverter.ToString(strResult);
        sResult = sResult.Replace("-", "");

        //小写转换输出加密字符串
        return sResult.ToLower();

        //大写转换输出加密字符串
        //return sResult.ToUpper();
    }

    /**
     * web 下载
     */
    public void DownloadBytes(string url, bool isAddRandomParams = true, Action<bool, byte[]> callback = null, Action<float> progress = null) {
        if (downloadWWW == null) {
            Init();
        }
        downloadWWW.DownloadBytes(url, isAddRandomParams, callback, progress, 3);
    }

    /**
     * 使用协程下载文件
     */
    public void WebTestDownload(string url, string filePath) {
        DownloadBytes(url, true, (ok, bytes) => {
            if (ok) {
                File.WriteAllBytes(filePath, bytes);
                Debug.Log("Download Success:" + filePath + "," + File.Exists(filePath));
            }
        });
    }

    /**
     * 连接 http 请求，解析返回内容
     */
    public string HttpGet(string url) {
        string str = null;
        HttpWebRequest res = (HttpWebRequest)WebRequest.Create(url);
        res.Method = "GET";
        res.ContentType = "text/html;charset=UTF-8";

        HttpWebResponse response = (HttpWebResponse)res.GetResponse();
        Stream strRes = response.GetResponseStream();
        StreamReader read = new StreamReader(strRes,Encoding.GetEncoding("utf-8"));
        str = read.ReadToEnd();
        Debug.Log(str);
        strRes.Close();
        read.Close();
        return str;
    }

    // Start is called before the first frame update
    void Start()
    {

    }

    // Update is called once per frame
    void Update()
    {
        
    }
}

```
## 3. 使用
```csharp
using System.Collections;
using System.Collections.Generic;
using UnityEngine;

public class ImageLoader : MonoBehaviour
{
    // Start is called before the first frame update
    void Start()
    {
        HttpManager httpManager = GetComponent<HttpManager>();
        string imageURL1 = "https://img.alicdn.com/imgextra/i3/O1CN01j1IjCp239bYwVoTxq_!!6000000007213-0-tps-1558-910.jpg";
        string imageURL2 = "https://ailabs.alibabausercontent.com/images/646336192abf5383cc42d9b4a564cf8b/1634560932170.png";
        // https://ailabs.alibabausercontent.com/images/4c78f7b58f4de12ed2cab9bcb9ec0ba0/1628491527458.png
        Debug.Log("Start " + httpManager);
        if (httpManager != null)
        {
            httpManager.LoadImage(imageURL1, GameObject.Find("RawImage"));
            httpManager.LoadImage(imageURL2, GameObject.Find("Plane"));
        }
    }

    // Update is called once per frame
    void Update()
    {
        
    }
}
```

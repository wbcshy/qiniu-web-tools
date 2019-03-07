package com.qiniu.storage;

import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.qiniu.common.Constants;
import com.qiniu.common.QiniuException;
import com.qiniu.http.Client;
import com.qiniu.http.HttpManager;
import com.qiniu.http.Response;
import com.qiniu.storage.Configuration;
import com.qiniu.storage.model.*;
import com.qiniu.util.*;

import java.util.*;

/**
 * 主要涉及了空间资源管理及批量操作接口的实现，具体的接口规格可以参考
 * 参考文档：<a href="http://developer.qiniu.com/kodo/api/rs">资源管理</a>
 */
public class BucketManager extends HttpManager {

    /**
     * Auth 对象
     * 该类需要使用QBox鉴权，所以需要指定Auth对象
     */
    private final Auth auth;

    /**
     * Configuration 对象
     * 该类相关的域名配置，解析配置，HTTP请求超时时间设置等
     */
    private Configuration configuration;

    /**
     * HTTP Client 对象
     * 该类需要通过该对象来发送HTTP请求
     */
    private final Client client;

    /**
     * 构建一个新的 BucketManager 对象
     *
     * @param auth Auth对象
     * @param cfg  Configuration对象
     */
    public BucketManager(Auth auth, Configuration cfg) {
        super(auth, cfg);
        this.auth = auth;
        this.configuration = cfg.clone();
        client = new Client(this.configuration);
    }

    public BucketManager(Auth auth, Client client) {
        super(auth, client);
        this.auth = auth;
        this.client = client;
    }

    /**
     * EncodedEntryURI格式，其中 bucket+":"+key 称之为 entry
     *
     * @param bucket
     * @param key
     * @return UrlSafeBase64.encodeToString(entry)
     * @link http://developer.qiniu.com/kodo/api/data-format
     */
    public static String encodedEntry(String bucket, String key) {
        String encodedEntry;
        if (key != null) {
            encodedEntry = UrlSafeBase64.encodeToString(bucket + ":" + key);
        } else {
            encodedEntry = UrlSafeBase64.encodeToString(bucket);
        }
        return encodedEntry;
    }

    /**
     * EncodedEntryURI格式，用在不指定key值的情况下
     *
     * @param bucket 空间名称
     * @return UrlSafeBase64.encodeToString(bucket)
     * @link http://developer.qiniu.com/kodo/api/data-format
     */
    public static String encodedEntry(String bucket) {
        return encodedEntry(bucket, null);
    }

    /**
     * 获取账号下所有空间名称列表
     *
     * @return 空间名称列表
     */
    public List<String> buckets() throws QiniuException {
        // 获取 bucket 列表 写死用rs.qiniu.com or rs.qbox.me @冯立元
        String url = String.format("%s/buckets", configuration.rsHost());
        Response res = get(url);
        if (!res.isOK()) {
            throw new QiniuException(res);
        }
        List<String> buckets = res.jsonToObject(List.class);
        res.close();
        return buckets;
    }

    public void createBucket(String bucketName, String region) throws QiniuException {
        String url = String.format("%s/mkbucketv2/%s/region/%s", configuration.rsHost(),
                UrlSafeBase64.encodeToString(bucketName), region);
        Response res = post(url, null);
        if (!res.isOK()) {
            throw new QiniuException(res);
        }
        res.close();
    }

    public void deleteBucket(String bucketname) throws QiniuException {
        String url = String.format("%s/drop/%s", configuration.rsHost(), bucketname);
        Response res = post(url, null);
        if (!res.isOK()) {
            throw new QiniuException(res);
        }
        res.close();
    }

    /**
     * 获取该空间下所有的domain
     *
     * @param bucket
     * @return 该空间名下的domain
     * @throws QiniuException
     */
    public String[] domainList(String bucket) throws QiniuException {
        String url = String.format("%s/v6/domain/list?tbl=%s", configuration.apiHost(), bucket);
        Response res = get(url);
        if (!res.isOK()) {
            throw new QiniuException(res);
        }
        String[] domains = res.jsonToObject(String[].class);
        res.close();
        return domains;
    }

    /**
     * 根据前缀获取文件列表的迭代器
     *
     * @param bucket 空间名
     * @param prefix 文件名前缀
     * @return FileInfo迭代器
     */
    public FileListIterator createFileListIterator(String bucket, String prefix) {
        return new FileListIterator(bucket, prefix, 1000, null);
    }

    /**
     * 根据前缀获取文件列表的迭代器
     *
     * @param bucket    空间名
     * @param prefix    文件名前缀
     * @param limit     每次迭代的长度限制，最大1000，推荐值 100
     * @param delimiter 指定目录分隔符，列出所有公共前缀（模拟列出目录效果）。缺省值为空字符串
     * @return FileInfo迭代器
     */
    public FileListIterator createFileListIterator(String bucket, String prefix, int limit, String delimiter) {
        return new FileListIterator(bucket, prefix, limit, delimiter);
    }

    private String listQuery(String bucket, String prefix, String marker, int limit, String delimiter) {
        StringMap map = new StringMap().put("bucket", bucket).putNotEmpty("marker", marker)
                .putNotEmpty("prefix", prefix).putNotEmpty("delimiter", delimiter).putWhen("limit", limit, limit > 0);
        return map.formString();
    }

    /**
     * 列举空间文件 v1 接口，返回一个 response 对象。
     *
     * @param bucket    空间名
     * @param prefix    文件名前缀
     * @param marker    上一次获取文件列表时返回的 marker
     * @param limit     每次迭代的长度限制，最大1000，推荐值 100
     * @param delimiter 指定目录分隔符，列出所有公共前缀（模拟列出目录效果）。缺省值为空字符串
     * @return
     * @throws QiniuException
     */
    public Response listV1(String bucket, String prefix, String marker, int limit, String delimiter)
            throws QiniuException {
        String url = String.format("%s/list?%s", configuration.rsfHost(auth.accessKey, bucket),
                listQuery(bucket, prefix, marker, limit, delimiter));
        return get(url);
    }

    public FileListing listFiles(String bucket, String prefix, String marker, int limit, String delimiter)
            throws QiniuException {
        Response response = listV1(bucket, prefix, marker, limit, delimiter);
        if (!response.isOK()) {
            throw new QiniuException(response);
        }
        FileListing fileListing = response.jsonToObject(FileListing.class);
        response.close();
        return fileListing;
    }

    /**
     * 列举空间文件 v2 接口，返回一个 response 对象。v2 接口可以避免由于大量删除导致的列举超时问题，返回的 response 对象中的 body 可以转换为
     * string stream 来处理。
     *
     * @param bucket    空间名
     * @param prefix    文件名前缀
     * @param marker    上一次获取文件列表时返回的 marker
     * @param limit     每次迭代的长度限制，推荐值 10000
     * @param delimiter 指定目录分隔符，列出所有公共前缀（模拟列出目录效果）。缺省值为空字符串
     * @return Response 返回一个 okhttp response 对象
     * @throws QiniuException
     */
    public Response listV2(String bucket, String prefix, String marker, int limit, String delimiter)
            throws QiniuException {
        String url = String.format("%s/v2/list?%s", configuration.rsfHost(auth.accessKey, bucket),
                listQuery(bucket, prefix, marker, limit, delimiter));
        return get(url);
    }

    public FileListing listFilesV2(String bucket, String prefix, String marker, int limit, String delimiter)
            throws QiniuException {
        Response response = listV2(bucket, prefix, marker, limit, delimiter);
        final String result = response.bodyString();
        response.close();
        List<String> lineList = Arrays.asList(result.split("\n"));
        FileListing fileListing = new FileListing();
        List<FileInfo> fileInfoList = new ArrayList<>();
        Set<String> commonPrefixSet = new HashSet<>();
        JsonObject jsonObject = new JsonObject();
        for (String line : lineList) {
            jsonObject = Json.decode(line, JsonObject.class);
            if (jsonObject == null) continue;
            if (!(jsonObject.get("item") instanceof JsonNull))
                fileInfoList.add(JsonConvertUtils.fromJson(jsonObject.get("item"), FileInfo.class));
            String dir = jsonObject.get("dir").getAsString();
            if (!"".equals(dir)) commonPrefixSet.add(dir);
        }
        fileListing.items = fileInfoList.toArray(new FileInfo[]{});
        fileListing.commonPrefixes = commonPrefixSet.toArray(new String[]{});
        fileListing.marker = jsonObject != null ? jsonObject.get("marker").getAsString() : "";
        return fileListing;
    }

    /**
     * 获取空间中文件的属性
     *
     * @param bucket  空间名称
     * @param fileKey 文件名称
     * @return 文件属性
     * @throws QiniuException
     * @link http://developer.qiniu.com/kodo/api/stat
     */
    public FileInfo stat(String bucket, String fileKey) throws QiniuException {
        Response res = statResponse(bucket, fileKey);
        if (!res.isOK()) {
            throw new QiniuException(res);
        }
        FileInfo fileInfo = res.jsonToObject(FileInfo.class);
        res.close();
        return fileInfo;
    }

    public Response statResponse(String bucket, String fileKey) throws QiniuException {
        return rsGet(bucket, String.format("/stat/%s", encodedEntry(bucket, fileKey)));
    }

    /**
     * 删除指定空间、文件名的文件
     *
     * @param bucket 空间名称
     * @param key    文件名称
     * @throws QiniuException
     * @link http://developer.qiniu.com/kodo/api/delete
     */
    public Response delete(String bucket, String key) throws QiniuException {
        return rsPost(bucket, String.format("/delete/%s", encodedEntry(bucket, key)), null);
    }

    /**
     * 修改文件的MimeType
     *
     * @param bucket 空间名称
     * @param key    文件名称
     * @param mime   文件的新MimeType
     * @throws QiniuException
     * @link http://developer.qiniu.com/kodo/api/chgm
     */
    public Response changeMime(String bucket, String key, String mime)
            throws QiniuException {
        String resource = encodedEntry(bucket, key);
        String encodedMime = UrlSafeBase64.encodeToString(mime);
        String path = String.format("/chgm/%s/mime/%s", resource, encodedMime);
        return rsPost(bucket, path, null);
    }

    /**
     * 修改文件的元数据
     *
     * @param bucket  空间名称
     * @param key     文件名称
     * @param headers 需要修改的文件元数据
     * @throws QiniuException
     * @link https://developer.qiniu.com/kodo/api/1252/chgm
     */
    public Response changeHeaders(String bucket, String key, Map<String, String> headers)
            throws QiniuException {
        String resource = encodedEntry(bucket, key);
        String path = String.format("/chgm/%s", resource);
        for (String k : headers.keySet()) {
            String encodedMetaValue = UrlSafeBase64.encodeToString(headers.get(k));
            path = String.format("%s/x-qn-meta-!%s/%s", path, k, encodedMetaValue);
        }
        return rsPost(bucket, path, null);
    }

    /**
     * 修改文件的类型（普通存储或低频存储）
     *
     * @param bucket 空间名称
     * @param key    文件名称
     * @param type   type=0 表示普通存储，type=1 表示低频存存储
     * @throws QiniuException
     */
    public Response changeType(String bucket, String key, StorageType type)
            throws QiniuException {
        String resource = encodedEntry(bucket, key);
        String path = String.format("/chtype/%s/type/%d", resource, type.ordinal());
        return rsPost(bucket, path, null);
    }

    /**
     * 修改文件的状态（禁用或者正常）
     *
     * @param bucket 空间名称
     * @param key    文件名称
     * @param status   0表示启用；1表示禁用。
     * @throws QiniuException
     */
    public Response changeStatus(String bucket, String key, int status)
            throws QiniuException {
        String resource = encodedEntry(bucket, key);
        String path = String.format("/chstatus/%s/status/%d", resource, status);
        return rsPost(bucket, path, null);
    }

    /**
     * 重命名空间中的文件，可以设置force参数为true强行覆盖空间已有同名文件
     *
     * @param bucket     空间名称
     * @param oldFileKey 文件名称
     * @param newFileKey 新文件名
     * @param force      强制覆盖空间中已有同名（和 newFileKey 相同）的文件
     * @throws QiniuException
     */
    public Response rename(String bucket, String oldFileKey, String newFileKey, boolean force)
            throws QiniuException {
        return move(bucket, oldFileKey, bucket, newFileKey, force);
    }

    /**
     * 重命名空间中的文件
     *
     * @param bucket     空间名称
     * @param oldFileKey 文件名称
     * @param newFileKey 新文件名
     * @throws QiniuException
     * @link http://developer.qiniu.com/kodo/api/move
     */
    public Response rename(String bucket, String oldFileKey, String newFileKey)
            throws QiniuException {
        return move(bucket, oldFileKey, bucket, newFileKey);
    }

    /**
     * 复制文件，要求空间在同一账号下，可以设置force参数为true强行覆盖空间已有同名文件
     *
     * @param fromBucket  源空间名称
     * @param fromFileKey 源文件名称
     * @param toBucket    目的空间名称
     * @param toFileKey   目的文件名称
     * @param force       强制覆盖空间中已有同名（和 toFileKey 相同）的文件
     * @throws QiniuException
     */
    public Response copy(String fromBucket, String fromFileKey, String toBucket, String toFileKey, boolean force)
            throws QiniuException {
        String from = encodedEntry(fromBucket, fromFileKey);
        String to = encodedEntry(toBucket, toFileKey);
        String path = String.format("/copy/%s/%s/force/%s", from, to, force);
        return rsPost(fromBucket, path, null);
    }

    /**
     * 复制文件，要求空间在同一账号下
     *
     * @param fromBucket  源空间名称
     * @param fromFileKey 源文件名称
     * @param toBucket    目的空间名称
     * @param toFileKey   目的文件名称
     * @throws QiniuException
     */
    public Response copy(String fromBucket, String fromFileKey, String toBucket, String toFileKey)
            throws QiniuException {
        Response res = copy(fromBucket, fromFileKey, toBucket, toFileKey, false);
        if (!res.isOK()) {
            throw new QiniuException(res);
        }
        res.close();
        return res;
    }

    /**
     * 移动文件，要求空间在同一账号下
     *
     * @param fromBucket  源空间名称
     * @param fromFileKey 源文件名称
     * @param toBucket    目的空间名称
     * @param toFileKey   目的文件名称
     * @param force       强制覆盖空间中已有同名（和 toFileKey 相同）的文件
     * @throws QiniuException
     */
    public Response move(String fromBucket, String fromFileKey, String toBucket,
                         String toFileKey, boolean force) throws QiniuException {
        String from = encodedEntry(fromBucket, fromFileKey);
        String to = encodedEntry(toBucket, toFileKey);
        String path = String.format("/move/%s/%s/force/%s", from, to, force);
        return rsPost(fromBucket, path, null);
    }

    /**
     * 移动文件。要求空间在同一账号下, 可以添加force参数为true强行移动文件。
     *
     * @param fromBucket  源空间名称
     * @param fromFileKey 源文件名称
     * @param toBucket    目的空间名称
     * @param toFileKey   目的文件名称
     * @throws QiniuException
     */
    public Response move(String fromBucket, String fromFileKey, String toBucket, String toFileKey)
            throws QiniuException {
        return move(fromBucket, fromFileKey, toBucket, toFileKey, false);
    }

    /**
     * 抓取指定地址的文件，以指定名称保存在指定空间
     * 要求指定url可访问，大文件不建议使用此接口抓取。可先下载再上传
     * 如果不指定保存的文件名，那么以文件内容的 etag 作为文件名
     *
     * @param url    待抓取的文件链接
     * @param bucket 文件抓取后保存的空间
     * @throws QiniuException
     */
    public FetchRet fetch(String url, String bucket) throws QiniuException {
        return fetch(url, bucket, null);
    }

    /**
     * 抓取指定地址的文件，以指定名称保存在指定空间
     * 要求指定url可访问，大文件不建议使用此接口抓取。可先下载再上传
     *
     * @param url    待抓取的文件链接
     * @param bucket 文件抓取后保存的空间
     * @param key    文件抓取后保存的文件名
     * @throws QiniuException
     */
    public FetchRet fetch(String url, String bucket, String key) throws QiniuException {
        String resource = UrlSafeBase64.encodeToString(url);
        String to = encodedEntry(bucket, key);
        String path = String.format("/fetch/%s/to/%s", resource, to);
        Response res = ioPost(bucket, path);
        if (!res.isOK()) {
            throw new QiniuException(res);
        }
        FetchRet fetchRet = res.jsonToObject(FetchRet.class);
        res.close();
        return fetchRet;
    }

    /**
     * 异步第三方资源抓取 从指定 URL 抓取资源，并将该资源存储到指定空间中。每次只抓取一个文件，抓取时可以指定保存空间名和最终资源名。
     * 主要对于大文件进行抓取
     * https://developer.qiniu.com/kodo/api/4097/asynch-fetch
     *
     * @param url    待抓取的文件链接，支持设置多个,以';'分隔
     * @param bucket 文件抓取后保存的空间
     * @param key    文件抓取后保存的文件名
     * @return Response
     * @throws QiniuException
     */
    public Response asynFetch(String url, String bucket, String key) throws QiniuException {
        String requesturl = configuration.apiHost(auth.accessKey, bucket) + "/sisyphus/fetch";
        StringMap stringMap = new StringMap().put("url", url).put("bucket", bucket).putNotNull("key", key);
        byte[] bodyByte = Json.encode(stringMap).getBytes(Constants.UTF_8);
        return client.post(requesturl, bodyByte,
                auth.authorizationV2(requesturl, "POST", bodyByte, "application/json"), Client.JsonMime);
    }

    /**
     * 异步第三方资源抓取 从指定 URL 抓取资源，并将该资源存储到指定空间中。每次只抓取一个文件，抓取时可以指定保存空间名和最终资源名。
     * https://developer.qiniu.com/kodo/api/4097/asynch-fetch
     * 提供更多参数的抓取 可以对抓取文件进行校验 和自定义抓取回调地址等
     *
     * @param url              待抓取的文件链接，支持设置多个,以';'分隔
     * @param bucket           文件抓取后保存的空间
     * @param key              文件抓取后保存的文件名
     * @param md5              文件md5,传入以后会在存入存储时对文件做校验，校验失败则不存入指定空间
     * @param etag             文件etag,传入以后会在存入存储时对文件做校验，校验失败则不存入指定空间
     * @param callbackurl      回调URL，详细解释请参考上传策略中的callbackUrl
     * @param callbackbody     回调Body，如果callbackurl不为空则必须指定。与普通上传一致支持魔法变量，
     * @param callbackbodytype 回调Body内容类型,默认为"application/x-www-form-urlencoded"，
     * @param callbackhost     回调时使用的Host
     * @param fileType         存储文件类型 0:正常存储(默认),1:低频存储
     * @return Response
     * @throws QiniuException
     */
    public Response asynFetch(String url, String bucket, String key, String md5, String etag,
                              String callbackurl, String callbackbody, String callbackbodytype,
                              String callbackhost, int fileType) throws QiniuException {
        String requesturl = configuration.apiHost(auth.accessKey, bucket) + "/sisyphus/fetch";
        StringMap stringMap = new StringMap().put("url", url).put("bucket", bucket).
                putNotNull("key", key).putNotNull("md5", md5).putNotNull("etag", etag).
                putNotNull("callbackurl", callbackurl).putNotNull("callbackbody", callbackbody).
                putNotNull("callbackbodytype", callbackbodytype).
                putNotNull("callbackhost", callbackhost).putNotNull("file_type", fileType);
        byte[] bodyByte = Json.encode(stringMap).getBytes(Constants.UTF_8);
        return client.post(requesturl, bodyByte,
                auth.authorizationV2(requesturl, "POST", bodyByte, "application/json"), Client.JsonMime);
    }

    /**
     * 查询异步抓取任务
     *
     * @param region      抓取任务所在bucket区域 华东 z0 华北 z1 华南 z2 北美 na0 东南亚 as0
     * @param fetchWorkId 抓取任务id
     * @return Response
     * @throws QiniuException
     */
    public Response checkAsynFetchid(String region, String fetchWorkId) throws QiniuException {
        String path = String.format("http://api-%s.qiniu.com/sisyphus/fetch?id=%s", region, fetchWorkId);
        return client.get(path, auth.authorization(path));
    }

    /**
     * 对于设置了镜像存储的空间，从镜像源站抓取指定名称的资源并存储到该空间中
     * 如果该空间中已存在该名称的资源，则会将镜像源站的资源覆盖空间中相同名称的资源
     *
     * @param bucket 空间名称
     * @param key    文件名称
     * @throws QiniuException
     */
    public void prefetch(String bucket, String key) throws QiniuException {
        String resource = encodedEntry(bucket, key);
        String path = String.format("/prefetch/%s", resource);
        Response res = ioPost(bucket, path);
        if (!res.isOK()) {
            throw new QiniuException(res);
        }
        res.close();
    }

    /**
     * 设置空间的镜像源站
     *
     * @param bucket     空间名称
     * @param srcSiteUrl 镜像回源地址
     */
    public Response setImage(String bucket, String srcSiteUrl) throws QiniuException {
        return setImage(bucket, srcSiteUrl, null);
    }

    /**
     * 设置空间的镜像源站
     *
     * @param bucket     空间名称
     * @param srcSiteUrl 镜像回源地址
     * @param host       镜像回源Host
     */
    public Response setImage(String bucket, String srcSiteUrl, String host) throws QiniuException {
        String encodedSiteUrl = UrlSafeBase64.encodeToString(srcSiteUrl);
        String encodedHost = null;
        if (host != null && host.length() > 0) {
            encodedHost = UrlSafeBase64.encodeToString(host);
        }
        String path = String.format("/image/%s/from/%s", bucket, encodedSiteUrl);
        if (encodedHost != null) {
            path += String.format("/host/%s", encodedHost);
        }
        return pubPost(path);
    }

    /**
     * 取消空间的镜像源站设置
     *
     * @param bucket 空间名称
     */
    public Response unsetImage(String bucket) throws QiniuException {
        String path = String.format("/unimage/%s", bucket);
        return pubPost(path);
    }



    /**
     * 设置文件的存活时间
     *
     * @param bucket 空间名称
     * @param key    文件名称
     * @param days   存活时间，单位：天
     */
    public Response deleteAfterDays(String bucket, String key, int days) throws QiniuException {
        return rsPost(bucket, String.format("/deleteAfterDays/%s/%d", encodedEntry(bucket, key), days), null);
    }

    public void setBucketAcl(String bucket, AclType acl) throws QiniuException {
        String url = String.format("%s/private?bucket=%s&private=%s", configuration.ucHost(), bucket, acl.getType());
        Response res = post(url, null);
        if (!res.isOK()) {
            throw new QiniuException(res);
        }
        res.close();
    }

    public BucketInfo getBucketInfo(String bucket) throws QiniuException {
        String url = String.format("%s/v2/bucketInfo?bucket=%s", configuration.ucHost(), bucket);
        Response res = post(url, null);
        if (!res.isOK()) {
            throw new QiniuException(res);
        }
        BucketInfo info = res.jsonToObject(BucketInfo.class);
        res.close();
        return info;
    }

    public void setIndexPage(String bucket, IndexPageType type) throws QiniuException {
        String url = String.format("%s/noIndexPage?bucket=%s&noIndexPage=%s",
                configuration.ucHost(), bucket, type.getType());
        Response res = post(url, null);
        if (!res.isOK()) {
            throw new QiniuException(res);
        }
        res.close();
    }



    /**
     * 批量文件管理请求
     */
    public Response batch(BatchOperations operations) throws QiniuException {
        return rsPost(operations.execBucket(), "/batch", operations.toBody());
    }

    /**
     * 文件管理批量操作指令构建对象
     */
    public static class BatchOperations {
        private ArrayList<String> ops;
        private String execBucket = null;

        public BatchOperations() {
            this.ops = new ArrayList<String>();
        }

        /**
         * 添加chgm指令
         */
        public BatchOperations addChgmOp(String bucket, String key, String newMimeType) {
            String resource = encodedEntry(bucket, key);
            String encodedMime = UrlSafeBase64.encodeToString(newMimeType);
            ops.add(String.format("/chgm/%s/mime/%s", resource, encodedMime));
            setExecBucket(bucket);
            return this;
        }

        /**
         * 添加copy指令
         */
        public BatchOperations addCopyOp(String fromBucket, String fromFileKey, String toBucket, String toFileKey) {
            String from = encodedEntry(fromBucket, fromFileKey);
            String to = encodedEntry(toBucket, toFileKey);
            ops.add(String.format("copy/%s/%s", from, to));
            setExecBucket(fromBucket);
            return this;
        }

        /**
         * 添加重命名指令
         */
        public BatchOperations addRenameOp(String fromBucket, String fromFileKey, String toFileKey) {
            return addMoveOp(fromBucket, fromFileKey, fromBucket, toFileKey);
        }

        /**
         * 添加move指令
         */
        public BatchOperations addMoveOp(String fromBucket, String fromKey, String toBucket, String toKey) {
            String from = encodedEntry(fromBucket, fromKey);
            String to = encodedEntry(toBucket, toKey);
            ops.add(String.format("move/%s/%s", from, to));
            setExecBucket(fromBucket);
            return this;
        }

        /**
         * 添加delete指令
         */
        public BatchOperations addDeleteOp(String bucket, String... keys) {
            for (String key : keys) {
                ops.add(String.format("delete/%s", encodedEntry(bucket, key)));
            }
            setExecBucket(bucket);
            return this;
        }

        /**
         * 添加stat指令
         */
        public BatchOperations addStatOps(String bucket, String... keys) {
            for (String key : keys) {
                ops.add(String.format("stat/%s", encodedEntry(bucket, key)));
            }
            setExecBucket(bucket);
            return this;
        }

        /**
         * 添加changeType指令
         */
        public BatchOperations addChangeTypeOps(String bucket, StorageType type, String... keys) {
            for (String key : keys) {
                ops.add(String.format("chtype/%s/type/%d", encodedEntry(bucket, key), type.ordinal()));
            }
            setExecBucket(bucket);
            return this;
        }

        /**
         * 添加changeStatus指令
         */
        public BatchOperations addChangeStatusOps(String bucket, int status, String... keys) {
            for (String key : keys) {
                ops.add(String.format("chstatus/%s/status/%d", encodedEntry(bucket, key), status));
            }
            setExecBucket(bucket);
            return this;
        }

        /**
         * 添加deleteAfterDays指令
         */
        public BatchOperations addDeleteAfterDaysOps(String bucket, int days, String... keys) {
            for (String key : keys) {
                ops.add(String.format("deleteAfterDays/%s/%d", encodedEntry(bucket, key), days));
            }
            setExecBucket(bucket);
            return this;
        }

        public byte[] toBody() {
            String body = StringUtils.join(ops, "&op=", "op=");
            return StringUtils.utf8Bytes(body);
        }

        public BatchOperations merge(BatchOperations batch) {
            this.ops.addAll(batch.ops);
            setExecBucket(batch.execBucket());
            return this;
        }

        public BatchOperations clearOps() {
            this.ops.clear();
            return this;
        }

        private void setExecBucket(String bucket) {
            if (execBucket == null) {
                execBucket = bucket;
            }
        }

        public String execBucket() {
            return execBucket;
        }
    }

    /**
     * 创建文件列表迭代器
     */
    public class FileListIterator implements Iterator<FileInfo[]> {
        private String marker = null;
        private String bucket;
        private String delimiter;
        private int limit;
        private String prefix;
        private QiniuException exception = null;

        public FileListIterator(String bucket, String prefix, int limit, String delimiter) {
            if (limit <= 0) {
                throw new IllegalArgumentException("limit must greater than 0");
            }
            if (limit > 1000) {
                throw new IllegalArgumentException("limit must not greater than 1000");
            }
            this.bucket = bucket;
            this.prefix = prefix;
            this.limit = limit;
            this.delimiter = delimiter;
        }

        public QiniuException error() {
            return exception;
        }

        @Override
        public boolean hasNext() {
            return exception == null && !"".equals(marker);
        }

        @Override
        public FileInfo[] next() {
            try {
                FileListing f = listFiles(bucket, prefix, marker, limit, delimiter);
                this.marker = f.marker == null ? "" : f.marker;
                return f.items;
            } catch (QiniuException e) {
                this.exception = e;
                return null;
            }
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException("remove");
        }
    }

}

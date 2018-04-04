
import okhttp3.internal.http2.ConnectionShutdownException;
import okhttp3.internal.http2.ErrorCode;
import okhttp3.internal.http2.StreamResetException;
import okio.BufferedSink;
import okio.Okio;
import okio.Sink;

/* 这个要被删除*/
/* 这个要被删除*/

/**
 * Created by 11111 2017/3/1.
 */

public class HttpBox {
    private static final String TAG = "HttpBox";

    private VSHttpClient /*这是要替换的*/mHttpClient;

    private static final int RETRY_MAX_TIMES = 1;//这是要消失的

    private int mRetryTimes = 0;
    /* 这个要被删除*/
    /*
     * correct
     */
    /* This is to left */
    // This will be left too.
    // 这个要被删除。
    /* 
     * 这个要被删除
     */
    public ResponseWriter getResponse(RequestReader requestReader) {
        ResponseWriter responseWriter = new ResponseWriter(requestReader);
        Response response = null;//这个要被替换删除
    }

}


import okhttp3.internal.http2.ConnectionShutdownException;
import okhttp3.internal.http2.ErrorCode;
import okhttp3.internal.http2.StreamResetException;
import okio.BufferedSink;
import okio.Okio;
import okio.Sink;

    /* 这个要被删除*/
    /* 这个要被删除*/

/**
 * Created by 11046763 on 2017/3/1.
 */

public class HttpBox {
    private static final String TAG = "HttpBox";

    private VSHttpClient /*这是要替换的*/mHttpClient;

    private static final int RETRY_MAX_TIMES = 1;

    private int mRetryTimes = 0;

    private boolean mResponseFail = false;

    public HttpBox(VSHttpClient client) {
        mHttpClient = client;
    }

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
        Response response = null;
        try {
            if (requestReader.hasSpeedyRequest()) {
                response = sendSpeedyRequest(responseWriter);
            } else {
                response = sendRequest(requestReader.getOriginalRequest());
            }
            if (response != null) {
                responseWriter.setTraceId(response.header(VSConstants.HEADER_TRACE_ID));
                responseWriter.setSpeedyRequest(true);
            }
        } catch (Exception e) {
            responseWriter.setSpeedyExceptionReason(e.getClass().toString());
            mResponseFail = true;
        }

        if (mResponseFail) {
            }
        }

        if (mResponseFail) {
            responseWriter.setResponse(null);
        } else {
            responseWriter.setResponse(response);
        }
        return responseWriter;
    }

    private Response getResponse(Request request, HttpCodec httpCodec, Handshake handshake,
            long connectStartMills) throws IOException {
        Response response;
                                  .build();

        return sendSpeedyRequest(request, true);
    }

    private Response sendSpeedyRequest(Request request, boolean isHeartRequest) throws IOException {
        Response response = null;
        int tryTimes = 0;
        boolean bLoop = true;
        while (bLoop) {
            boolean releaseConnection = true;
                    throw new ProtocolException("HTTP " + code
                            + " had non-zero Content-Length: " + response.body().contentLength());
                }
                releaseConnection = false;
            } catch (IOException e) {
                VIVOLog.w(TAG,
                        "LongConnection IOException: " + request.url() + "; tryTimes = " + tryTimes
                                + ";EX =" + e.getClass().toString() + e);

                // An attempt to communicate with a server failed. The request may have been sent.
                boolean requestSendStarted = !(e instanceof ConnectionShutdownException);
                if (!recoverLongConnection(e, requestSendStarted, request, tryTimes)) {
                    throw e;
                }
                releaseConnection = false;
                tryTimes++;
                continue;
            } catch (RouteException e) {
                VIVOLog.w(TAG,
                        "LongConnection RouteException: " + request.url() + "; tryTimes = "
                                + tryTimes + ";EX =" + e.getClass().toString() + e);

                // The attempt to connect via a route failed. The request will not have been sent.
                if (!recoverLongConnection(e.getLastConnectException(), false, request, tryTimes)) {
                    throw e.getLastConnectException();
                }
                releaseConnection = false;
                tryTimes++;
                continue;
            } catch (Exception e) {
                VIVOLog.w(TAG, "LongConnection Exception: " + request.url() + e);
                e.printStackTrace();
                throw e;
            } finally {
                if (releaseConnection) {
                    LongConnManager.getInstance().releaseConnection();
                    //释放长连接后，及时重新建立长连接
                    LongConnManager.getInstance().resumeConnection();
                    mResponseFail = true;
                }
            }

            bLoop = false;
        }

        return response;
    }

    private Response sendSpeedyRequest(ResponseWriter responseWriter) throws IOException {
        Request request = responseWriter.getSpeedyRequest();
        Response response = sendSpeedyRequest(request, false);

        if (response != null && request.url().host().equalsIgnoreCase(VSConstants.URL_PROXY_HOST)) {
            String proxy_code = response.header(VSConstants.HEADER_PROXY_CODE);

            int code = toProxyCode(proxy_code);
            if (code != VSConstants.PCODE_OK) {
            }
        }
        return response;
    }

    /**
     * Report and attempt to recover from a failure to communicate with a server. Returns true if
     * {@code e} is recoverable, or false if the failure is permanent. Requests with a body can only
     * be recovered if the body is buffered or if the failure occurred before the request has been
     * sent.
     */
    private boolean recoverLongConnection(
            IOException e, boolean requestSendStarted, Request userRequest, int tryTimes) {
        // We can't send the request body again.
        if (requestSendStarted && userRequest.body() instanceof UnrepeatableRequestBody)
            return false;

        // This exception is fatal.
        if (!isLongConnectionRecoverable(e)) return false;

        // No more routes to attempt.
        if (tryTimes >= 1) return false;

        // For failure recovery, use the same route selector with a new connection.
        return true;
    }

    private boolean isLongConnectionRecoverable(IOException e) {
        if (e instanceof StreamResetException) {
            // On HTTP/2 stream errors, retry REFUSED_STREAM errors once on the same connection. All
            // other errors must be retried on a new connection.

            StreamResetException streamResetException = (StreamResetException) e;
            if (streamResetException.errorCode != ErrorCode.REFUSED_STREAM) {
                return false;
            }
        } else if (e instanceof ConnectionShutdownException) {
            return false;
        } else if (e instanceof ProtocolException) {
            // If there was a protocol problem, don't recover.
            return false;
        } else if (e instanceof SSLHandshakeException) {
            //            // Look for known client-side or negotiation errors that are unlikely to
            //            be fixed by trying
            //            // again with a different route.
            //
            //            // If the problem was a CertificateException from the X509TrustManager,
            //            // do not retry.
            //            if (e.getCause() instanceof CertificateException) {
            //                return false;
            //            }
            // no other route
            return false;
        } else if (e instanceof SSLPeerUnverifiedException) {
            // e.g. a certificate pinning error.
            return false;
        }

        // An example of one we might want to retry with a different route is a problem connecting
        // to a proxy and would manifest as a standard IOException. Unless it is one we know we
        // should not retry, we return true and try a new route.
        return true;
    }

    private Response sendRequest(Request request) throws IOException {
        Response response = null;
        StreamAllocation streamAllocation = new StreamAllocation(mHttpClient.connectionPool(),
                mHttpClient.createAddress(request.url()), mHttpClient.getStackTraceForCloseable());

        boolean bLoop = true;
        while (bLoop) {
            boolean releaseConnection = true;
            try {
                boolean doExtensiveHealthChecks = !request.method().equals(HttpMethod.GET);
                long connectStartMills = System.currentTimeMillis();
                HttpCodec httpCodec = streamAllocation.newStream(
                        mHttpClient.getOKHttpClient(), doExtensiveHealthChecks);
                response = getResponse(request, httpCodec,
                        streamAllocation.connection().handshake(), connectStartMills);
                int code = response.code();

                if (VSConstants.HEADER_VALUE_CLOSE.equalsIgnoreCase(
                            response.request().header(VSConstants.HEADER_CONNECTION))
                        || VSConstants.HEADER_VALUE_CLOSE.equalsIgnoreCase(
                                   response.header(VSConstants.HEADER_CONNECTION))) {
                    streamAllocation.noNewStreams();
                }

                if ((code == 204 || code == 205) && response.body().contentLength() > 0) {
                    throw new ProtocolException("HTTP " + code
                            + " had non-zero Content-Length: " + response.body().contentLength());
                }
                releaseConnection = false;
            } catch (IOException e) {
                // An attempt to communicate with a server failed. The request may have been sent.
                boolean requestSendStarted = !(e instanceof ConnectionShutdownException);
                if (!recover(streamAllocation, e, requestSendStarted, request)) {
                    throw e;
                }

                releaseConnection = false;
                continue;

            } catch (RouteException e) {
                // VIVOLog.w(TAG, "get data RouteException: " + request.url());
                if (!recover(streamAllocation, e.getLastConnectException(), true, request)) {
                    throw e.getLastConnectException();
                }
                releaseConnection = false;
                continue;

            } catch (Exception e) {
                VIVOLog.w(TAG, "get data Exception: " + request.url());
                throw e;
            } finally {
                if (releaseConnection) {
                    streamAllocation.streamFailed(null);
                    streamAllocation.release();
                    mResponseFail = true;
                }
            }

            streamAllocation.release();
            bLoop = false;
        }

        return response;
    }

    private int toProxyCode(String proxy_code) throws NumberFormatException {
        if (TextUtils.isEmpty(proxy_code)) {
            return VSConstants.PCODE_SERVER_ERROR;
        }
        return Integer.parseInt(proxy_code);
    }

    /**
     * Report and attempt to recover from a failure to communicate with a server. Returns true if
     * {@code e} is recoverable, or false if the failure is permanent. Requests with a body can only
     * be recovered if the body is buffered.
     */
    protected boolean recover(StreamAllocation streamAllocation, IOException e,
            boolean routeException, Request userRequest) {
        streamAllocation.streamFailed(e);

        // The application layer has forbidden retries.
        if (!mHttpClient.retryOnConnectionFailure()) {
            return false;
        }

        // We can't send the request body again.
        if (!routeException && userRequest.body() instanceof UnrepeatableRequestBody) {
            return false;
        }

        // This exception is fatal.
        if (!isRecoverable(e, routeException)) {
            return false;
        }

        // No more routes to attempt.
        if (!streamAllocation.hasMoreRoutes()) {
            return false;
        }

        // For failure recovery, use the same route selector with a new connection.
        return true;
    }

    private boolean isRecoverable(IOException e, boolean routeException) {
        // If there was a protocol problem, don't recover.
        if (e instanceof ProtocolException) {//这个条件有问题？
            return false;
        }

        // If there was an interruption don't recover, but if there was a timeout connecting to a
        // route we should try the next route (if there is one).
        if (e instanceof InterruptedIOException) {
            return e instanceof SocketTimeoutException && routeException;
        }

        // Look for known client-side or negotiation errors that are unlikely to be fixed by trying
        // again with a different route.
        if (e instanceof SSLHandshakeException) {
            // If the problem was a CertificateException from the X509TrustManager,
            // do not retry.
            if (e.getCause() instanceof CertificateException) {
                return false;
            }
        }
        if (e instanceof SSLPeerUnverifiedException) {
            // e.g. a certificate pinning error.
            return false;
        }

        // An example of one we might want to retry with a different route is a problem connecting
        // to a proxy and would manifest as a standard IOException. Unless it is one we know we
        // should not retry, we return true and try a new route.
        return true;
    }
}

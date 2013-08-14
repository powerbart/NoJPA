package dk.lessismore.reusable_v4.webservicelog;

public interface CallerIdentifierLogService {


    public CallerIdentifierLog newLocalInstance(String methodName);

    public CallerIdentifierLog newInstance(
            String loginUserName, String systemUserName, String callerApplicationName,
            String callerMethodName, String callerHostName, String callerVersionName 
            );


}

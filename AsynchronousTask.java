package my.airo.infra.service;

import org.apache.log4j.Logger;

public abstract class AsynchronousTask {

    protected Logger logger = Logger.getLogger(AsynchronousTask.class);

    public abstract void body() throws Exception;

    private UserOperationContextService userOperationContextService;

    public AsynchronousTask(UserOperationContextService userOperationContextService) {
        this.userOperationContextService = userOperationContextService;
    }

    public void exceptionCallback(Exception e) {
        userOperationContextService.warn(e);
    }

    public void execute() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    body();
                } catch (Exception e) {
                    exceptionCallback(e);
                }
            }
        }).start();
    }

    public void execute(final AsyncCallback callback) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    body();
                    callback.execute();
                } catch (Exception e) {
                    exceptionCallback(e);
                }
            }
        }).start();
    }

}

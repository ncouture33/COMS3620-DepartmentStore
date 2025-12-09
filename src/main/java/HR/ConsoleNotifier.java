package HR;

public class ConsoleNotifier implements  INotificationService {
    @Override
    public void notifySuccess(String message){
        System.out.println("[SUCCESS]" + message);
    }
    @Override
    public void notifyFailure(String message){
        System.out.println("[FAILURE]" + message);
    }
}

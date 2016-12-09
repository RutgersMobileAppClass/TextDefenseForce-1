package rutgers.text_defense_force.word_list_service.service_wrapper;

public interface ServiceWrapperListener {

    public void wordObtained(String word);
    public void serviceFailure();
}
package rutgers.text_defense_force.word_list_service.cached_service_wrapper;

public interface CachedServiceWrapperListener {

    public void wordObtained(String word);
    public void serviceFailure();

}
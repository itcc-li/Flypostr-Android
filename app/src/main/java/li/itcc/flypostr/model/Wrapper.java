package li.itcc.flypostr.model;

/**
 * Created by sandro.pedrett on 20.08.2016.
 */

abstract class Wrapper<T> {
    protected T bean;

    public Wrapper() {

    }

    public Wrapper(T bean) {
        this.bean = bean;
    }

    public T getBean() {
        return bean;
    }
}

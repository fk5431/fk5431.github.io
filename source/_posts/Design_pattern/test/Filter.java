package factory.pattern.filter_pattern;

import java.util.List;

/**
 * Created by fk5431 on 7/27/17.
 */
public interface Filter {
    List<Person> filter(List<Person> persions);
}

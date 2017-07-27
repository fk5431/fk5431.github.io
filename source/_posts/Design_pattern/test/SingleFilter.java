package factory.pattern.filter_pattern;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by fk5431 on 7/27/17.
 */
public class SingleFilter implements Filter {
    @Override
    public List<Person> filter(List<Person> persions) {
        List<Person> result = new ArrayList<Person>();
        for(Person p : persions){
            if ("SINGLE".equalsIgnoreCase(p.getMarital())){
                result.add(p);
            }
        }
        return result;
    }
}

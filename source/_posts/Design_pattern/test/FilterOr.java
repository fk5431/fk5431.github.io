package factory.pattern.filter_pattern;

import java.util.List;

/**
 * Created by fk5431 on 7/27/17.
 */
public class FilterOr implements Filter {
    private Filter filter;
    private Filter otherfilter;

    public FilterOr(Filter filter, Filter otherfilter){
        this.filter = filter;
        this.otherfilter = otherfilter;
    }

    @Override
    public List<Person> filter(List<Person> persions) {
        List<Person> tmpList = filter.filter(persions);
        List<Person> tmpList2 = otherfilter.filter(persions);
        for(Person p : tmpList2){
            if(!tmpList.contains(p)){
                tmpList.add(p);
            }
        }
        return tmpList;
    }
}

package factory.pattern.filter_pattern;

import java.util.List;

/**
 * Created by fk5431 on 7/27/17.
 */
public class FilterAnd implements Filter {
    private Filter filter;
    private Filter otherfilter;

    public FilterAnd(Filter filter, Filter otherfilter){
        this.filter = filter;
        this.otherfilter = otherfilter;
    }

    @Override
    public List<Person> filter(List<Person> persions) {
        List<Person> tmpList = filter.filter(persions);
        return otherfilter.filter(tmpList);
    }
}

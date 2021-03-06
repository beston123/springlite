package org.springlite.beans.exception;

import org.springlite.util.StringUtils;

import java.util.Arrays;
import java.util.Collection;

/**
 * Created by swy on 2016/5/25.
 */
@SuppressWarnings("serial")
public class NoUniqueBeanDefinitionException extends NoSuchBeanDefinitionException {

    private int numberOfBeansFound;


    /**
     * Create a new {@code NoUniqueBeanDefinitionException}.
     * @param type required type of the non-unique bean
     * @param numberOfBeansFound the number of matching beans
     * @param message detailed message describing the problem
     */
    public NoUniqueBeanDefinitionException(Class<?> type, int numberOfBeansFound, String message) {
        super(type, message);
        this.numberOfBeansFound = numberOfBeansFound;
    }

    /**
     * Create a new {@code NoUniqueBeanDefinitionException}.
     * @param type required type of the non-unique bean
     * @param beanNamesFound the names of all matching beans (as a Collection)
     */
    public NoUniqueBeanDefinitionException(Class<?> type, Collection<String> beanNamesFound) {
        this(type, beanNamesFound.size(), "expected single matching bean but found " + beanNamesFound.size() + ": " +
                StringUtils.collectionToCommaDelimitedString(beanNamesFound));
    }

    /**
     * Create a new {@code NoUniqueBeanDefinitionException}.
     * @param type required type of the non-unique bean
     * @param beanNamesFound the names of all matching beans (as an array)
     */
    public NoUniqueBeanDefinitionException(Class<?> type, String... beanNamesFound) {
        this(type, Arrays.asList(beanNamesFound));
    }


    /**
     * Return the number of beans found when only one matching bean was expected.
     * For a NoUniqueBeanDefinitionException, this will usually be higher than 1.
     * @see #getBeanType()
     */
    @Override
    public int getNumberOfBeansFound() {
        return this.numberOfBeansFound;
    }

}


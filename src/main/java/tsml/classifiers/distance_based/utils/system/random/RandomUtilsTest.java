package tsml.classifiers.distance_based.utils.system.random;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import utilities.ArrayUtilities;
import weka.core.Debug;

import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static tsml.classifiers.distance_based.utils.collections.CollectionUtils.newArrayList;

public class RandomUtilsTest {


    private List<Integer> list;
    private Random random;

    @Before
    public void before() {
        random = new Random(0);
        list = IntStream.rangeClosed(1,10).boxed().collect(Collectors.toList());
    }

    @Test
    public void testRandomChoiceSingle() {
        int choice = RandomUtils.choice(list, random);
        Assert.assertEquals(1, choice);
        choice = RandomUtils.choice(list, random);
        Assert.assertEquals(9, choice);
        choice = RandomUtils.choice(list, random);
        Assert.assertEquals(10, choice);
        choice = RandomUtils.choice(list, random);
        Assert.assertEquals(8, choice);
        choice = RandomUtils.choice(list, random);
        Assert.assertEquals(6, choice);
        Assert.assertEquals(10, list.size());
    }

    @Test
    public void testRandomPickSingle() {
        int choice = RandomUtils.pick(list, random);
        Assert.assertEquals(1, choice);
        Assert.assertFalse(list.contains(choice));
        Assert.assertEquals(9, list.size());

        choice = RandomUtils.pick(list, random);
        Assert.assertEquals(9, choice);
        Assert.assertFalse(list.contains(choice));
        Assert.assertEquals(8, list.size());

        choice = RandomUtils.pick(list, random);
        Assert.assertEquals(3, choice);
        Assert.assertFalse(list.contains(choice));
        Assert.assertEquals(7, list.size());

        choice = RandomUtils.pick(list, random);
        Assert.assertEquals(5, choice);
        Assert.assertFalse(list.contains(choice));
        Assert.assertEquals(6, list.size());

        choice = RandomUtils.pick(list, random);
        Assert.assertEquals(10, choice);
        Assert.assertFalse(list.contains(choice));
        Assert.assertEquals(5, list.size());
    }

    @Test
    public void testRandomChoiceMultiple() {
        final List<Integer> choice = RandomUtils.choice(list, random, 5);
        Assert.assertEquals(5, choice.size());
        Assert.assertEquals(new Integer(1), choice.get(0));
        Assert.assertEquals(new Integer(8), choice.get(1));
        Assert.assertEquals(new Integer(2), choice.get(2));
        Assert.assertEquals(new Integer(3), choice.get(3));
        Assert.assertEquals(new Integer(6), choice.get(4));
        Assert.assertEquals(10, list.size());
    }

    @Test
    public void testRandomPickMultiple() {
        final List<Integer> choice = RandomUtils.pick(list, random, 5);
        Assert.assertEquals(5, choice.size());
        Assert.assertEquals(new Integer(1), choice.get(0));
        Assert.assertEquals(new Integer(8), choice.get(1));
        Assert.assertEquals(new Integer(2), choice.get(2));
        Assert.assertEquals(new Integer(3), choice.get(3));
        Assert.assertEquals(new Integer(6), choice.get(4));
        Assert.assertTrue(list.contains(4));
        Assert.assertTrue(list.contains(5));
        Assert.assertTrue(list.contains(7));
        Assert.assertTrue(list.contains(9));
        Assert.assertTrue(list.contains(10));
        Assert.assertTrue(list.size() == 5);
    }

    @Test
    public void testRandomChoiceAll() {
        final List<Integer> choice = RandomUtils.choice(list, random, 10);
        Assert.assertEquals(list.size(), choice.size());
        Assert.assertEquals(new Integer(1), choice.get(0));
        Assert.assertEquals(new Integer(8), choice.get(1));
        Assert.assertEquals(new Integer(2), choice.get(2));
        Assert.assertEquals(new Integer(3), choice.get(3));
        Assert.assertEquals(new Integer(6), choice.get(4));
        Assert.assertEquals(new Integer(4), choice.get(5));
        Assert.assertEquals(new Integer(7), choice.get(6));
        Assert.assertEquals(new Integer(10), choice.get(7));
        Assert.assertEquals(new Integer(9), choice.get(8));
        Assert.assertEquals(new Integer(5), choice.get(9));
        Assert.assertEquals(10, list.size());
    }

    @Test
    public void testRandomPickAll() {
        final List<Integer> choice = RandomUtils.pick(list, random, 10);
        Assert.assertEquals(0, list.size());
        Assert.assertEquals(10, choice.size());
        Assert.assertEquals(new Integer(1), choice.get(0));
        Assert.assertEquals(new Integer(8), choice.get(1));
        Assert.assertEquals(new Integer(2), choice.get(2));
        Assert.assertEquals(new Integer(3), choice.get(3));
        Assert.assertEquals(new Integer(6), choice.get(4));
        Assert.assertEquals(new Integer(4), choice.get(5));
        Assert.assertEquals(new Integer(7), choice.get(6));
        Assert.assertEquals(new Integer(10), choice.get(7));
        Assert.assertEquals(new Integer(9), choice.get(8));
        Assert.assertEquals(new Integer(5), choice.get(9));
        Assert.assertTrue(list.isEmpty());
    }

    @Test
    public void testRandomChoiceWithReplacement() {
        final List<Integer> choice = RandomUtils.choiceWithReplacement(list, random, list.size() * 10);
        final Set<Integer> set = new HashSet<>(choice);
        Assert.assertEquals(list.size(), set.size());
        Assert.assertEquals(10, list.size());
    }

    @Test
    public void testRandomChoiceWithoutReplacement() {
        final List<Integer> choice = RandomUtils.choice(list, random, 10);
        final Set<Integer> set = new HashSet<>(choice);
        Assert.assertEquals(choice.size(), set.size());
        Assert.assertEquals(10, list.size());
    }

    @Test
    public void testRandomPickWithReplacement() {
        final List<Integer> choice = RandomUtils.pickWithReplacement(list, random, list.size() * 10);
        final Set<Integer> set = new HashSet<>(choice);
        Assert.assertEquals(10, set.size());
        Assert.assertEquals(0, list.size());
    }

    @Test
    public void testRandomPickWithoutReplacement() {
        final List<Integer> choice = RandomUtils.pick(list, random, 10);
        final Set<Integer> set = new HashSet<>(choice);
        Assert.assertEquals(choice.size(), set.size());
        Assert.assertEquals(0, list.size());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testRandomChoiceTooMany() {
        RandomUtils.choice(list, random, list.size() + 1);
    }

}

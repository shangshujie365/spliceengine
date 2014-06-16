package com.splicemachine.encoding;

import com.google.common.collect.Lists;
import org.apache.hadoop.hbase.util.Bytes;
import org.junit.Test;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class BigDecimalEncoding_SortTest {

    @Test
    public void sort_positiveValue_negativeExponents() {
        List<BigDecimal> bigDecimalList = Lists.newArrayList(
                new BigDecimal(".5"),
                new BigDecimal(".55"),
                new BigDecimal(".555"),
                new BigDecimal(".5555"),
                new BigDecimal(".55555"),
                new BigDecimal(".555555"),
                new BigDecimal(".5555555"),
                new BigDecimal(".55555555"),
                new BigDecimal(".555555555"),
                new BigDecimal(".5555555555"),
                new BigDecimal(".55555555555"),
                new BigDecimal(".555555555555"),
                new BigDecimal(".5555555555555"),
                new BigDecimal(".55555555555555"),
                new BigDecimal(".555555555555555"),
                new BigDecimal(".5555555555555555"),
                new BigDecimal(".5555555555555555555555555555555555")
        );
        assertSort(bigDecimalList);
    }

    @Test
    public void sort_positiveValue_positiveExponents() {
        List<BigDecimal> bigDecimalList = Lists.newArrayList(
                new BigDecimal("5"),
                new BigDecimal("55"),
                new BigDecimal("555"),
                new BigDecimal("5555"),
                new BigDecimal("55555"),
                new BigDecimal("555555"),
                new BigDecimal("5555555"),
                new BigDecimal("55555555"),
                new BigDecimal("555555555"),
                new BigDecimal("5555555555"),
                new BigDecimal("55555555555"),
                new BigDecimal("555555555555"),
                new BigDecimal("5555555555555"),
                new BigDecimal("55555555555555"),
                new BigDecimal("555555555555555"),
                new BigDecimal("5555555555555555"),
                new BigDecimal("5555555555555555555555555555555555")
        );
        assertSort(bigDecimalList);
    }

    @Test
    public void sort_negativeValue_negativeExponents() {
        List<BigDecimal> bigDecimalList = Lists.newArrayList(
                new BigDecimal("-.5555555555555555555555555555555555"),
                new BigDecimal("-.5555555555555555"),
                new BigDecimal("-.555555555555555"),
                new BigDecimal("-.55555555555555"),
                new BigDecimal("-.5555555555555"),
                new BigDecimal("-.555555555555"),
                new BigDecimal("-.55555555555"),
                new BigDecimal("-.5555555555"),
                new BigDecimal("-.555555555"),
                new BigDecimal("-.55555555"),
                new BigDecimal("-.5555555"),
                new BigDecimal("-.555555"),
                new BigDecimal("-.55555"),
                new BigDecimal("-.5555"),
                new BigDecimal("-.555"),
                new BigDecimal("-.55"),
                new BigDecimal("-.5")
        );
        assertSort(bigDecimalList);
    }

    @Test
    public void sort_negativeValue_positiveExponents() {
        List<BigDecimal> bigDecimalList = Lists.newArrayList(
                new BigDecimal("-5555555555555555555555555555555555"),
                new BigDecimal("-5555555555555555"),
                new BigDecimal("-555555555555555"),
                new BigDecimal("-55555555555555"),
                new BigDecimal("-5555555555555"),
                new BigDecimal("-555555555555"),
                new BigDecimal("-55555555555"),
                new BigDecimal("-5555555555"),
                new BigDecimal("-555555555"),
                new BigDecimal("-55555555"),
                new BigDecimal("-5555555"),
                new BigDecimal("-555555"),
                new BigDecimal("-55555"),
                new BigDecimal("-5555"),
                new BigDecimal("-555"),
                new BigDecimal("-55"),
                new BigDecimal("-5")
        );
        assertSort(bigDecimalList);
    }

    @Test
    public void sort_positiveValues_positiveExponents_AND_negativeExponents() {
        List<BigDecimal> bigDecimalList = Lists.newArrayList(
                new BigDecimal(".5"),
                new BigDecimal(".55"),
                new BigDecimal(".555"),
                new BigDecimal(".5555"),
                new BigDecimal(".55555"),
                new BigDecimal(".555555"),
                new BigDecimal(".5555555"),
                new BigDecimal(".55555555"),
                new BigDecimal(".555555555"),
                new BigDecimal("5"),
                new BigDecimal("55"),
                new BigDecimal("555"),
                new BigDecimal("5555"),
                new BigDecimal("55555"),
                new BigDecimal("555555"),
                new BigDecimal("5555555"),
                new BigDecimal("55555555")
        );
        assertSort(bigDecimalList);
    }

    @Test
    public void sort_negativeValues_positiveExponents_AND_negativeExponents() {
        List<BigDecimal> bigDecimalList = Lists.newArrayList(
                new BigDecimal("-55555555"),
                new BigDecimal("-5555555"),
                new BigDecimal("-555555"),
                new BigDecimal("-55555"),
                new BigDecimal("-5555"),
                new BigDecimal("-555"),
                new BigDecimal("-55"),
                new BigDecimal("-5"),
                new BigDecimal("-.555555555"),
                new BigDecimal("-.55555555"),
                new BigDecimal("-.5555555"),
                new BigDecimal("-.555555"),
                new BigDecimal("-.55555"),
                new BigDecimal("-.5555"),
                new BigDecimal("-.555"),
                new BigDecimal("-.55"),
                new BigDecimal("-.5")
        );
        assertSort(bigDecimalList);
    }


    public void assertSort(List<BigDecimal> originalList) {
        List<BigDecimal> ascendingList = Lists.newArrayList(originalList);
        List<BigDecimal> descendingList = Lists.newArrayList(originalList);

        Collections.sort(ascendingList);
        Collections.reverse(descendingList);

        // assert original list (test data) was in ascending order
        assertEquals(originalList, ascendingList);

        assertStuff(ascendingList, false);
        assertStuff(descendingList, true);
    }

    public void assertStuff(List<BigDecimal> bigDecimalList, boolean descending) {
        List<byte[]> encodedBigDecimalsBytes = EncodingTestUtil.toBytes(bigDecimalList, descending);
        Collections.shuffle(encodedBigDecimalsBytes);
        Collections.sort(encodedBigDecimalsBytes, Bytes.BYTES_COMPARATOR);
        List<BigDecimal> decodedList = EncodingTestUtil.toBigDecimal(encodedBigDecimalsBytes, descending);
        assertEquals("descending=" + descending, bigDecimalList, decodedList);
    }


}

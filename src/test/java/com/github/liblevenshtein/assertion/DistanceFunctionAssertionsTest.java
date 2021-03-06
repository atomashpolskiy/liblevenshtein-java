package com.github.liblevenshtein.assertion;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.github.liblevenshtein.transducer.DistanceFunction;
import com.github.liblevenshtein.transducer.State;

import static com.github.liblevenshtein.assertion.DistanceFunctionAssertions.assertThat;

public class DistanceFunctionAssertionsTest {

  private final ThreadLocal<DistanceFunction> distance = new ThreadLocal<>();

  private final ThreadLocal<State> state = new ThreadLocal<>();

  @BeforeMethod
  public void setUp() {
    distance.set(mock(DistanceFunction.class));
    state.set(mock(State.class));
  }

  @Test
  public void testHasDistance() {
    when(distance.get().at(state.get(), 4)).thenReturn(2);
    assertThat(distance.get()).hasDistance(state.get(), 4, 2);
  }

  @Test(expectedExceptions = AssertionError.class)
  public void testHasDistanceAgainstViolation() {
    when(distance.get().at(state.get(), 4)).thenReturn(2);
    assertThat(distance.get()).hasDistance(state.get(), 4, 3);
  }
}

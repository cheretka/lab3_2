package edu.iis.mto.time;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.List;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;
import java.lang.reflect.InvocationHandler;



@ExtendWith(MockitoExtension.class)
class OrderTest {

    private static final long VALID_PERIOD_HOURS = 20;
    private static final long UNVALID_PERIOD_HOURS = 40;

    @Mock
    private Clock variableClock;

    private Order order;
    private Instant startTimeCount;
    private Instant endTimeCount;

    @BeforeEach
    void setUp() throws Exception {
        order = new Order(variableClock);
        startTimeCount = Instant.now();

        when(variableClock.getZone())
                .thenReturn(ZoneId.systemDefault());
    }

    @Test
    void when_ElapsedZeroSeconds_Expect_ConfirmedOrderState() {
        endTimeCount = startTimeCount;

        when(variableClock.instant())
                .thenReturn(startTimeCount)
                .thenReturn(endTimeCount);

        order.submit();

        assertDoesNotThrow(() -> order.confirm());
        assertEquals(order.getOrderState(), Order.State.CONFIRMED);
    }

    @Test
    void when_ElapsedValidHours_Expect_ConfirmedOrderState() {
        endTimeCount = startTimeCount.plus(VALID_PERIOD_HOURS, ChronoUnit.HOURS);

        when(variableClock.instant())
                .thenReturn(startTimeCount)
                .thenReturn(endTimeCount);

        order.submit();

        assertDoesNotThrow(() -> order.confirm());
        assertEquals(order.getOrderState(), Order.State.CONFIRMED);
    }

    @Test
    void when_ElapsedOneSecondBeforeInvalidHours_Expect_ConfirmedOrderState() {
        endTimeCount = startTimeCount.plus(24, ChronoUnit.HOURS).plus(59, ChronoUnit.MINUTES).plus(59, ChronoUnit.SECONDS);

        when(variableClock.instant())
                .thenReturn(startTimeCount)
                .thenReturn(endTimeCount);

        order.submit();

        assertDoesNotThrow(() -> order.confirm());
        assertEquals(order.getOrderState(), Order.State.CONFIRMED);
    }

    @Test
    void when_ReachedThresholdOfValidHours_Expect_CancelledOrderState_And_ThrowException() {
        endTimeCount = startTimeCount.plus(25, ChronoUnit.HOURS);

        when(variableClock.instant())
                .thenReturn(startTimeCount)
                .thenReturn(endTimeCount);

        order.submit();

        assertThrows(OrderExpiredException.class, () -> order.confirm());
        assertEquals(order.getOrderState(), Order.State.CANCELLED);
    }

    @Test
    void when_ElapsedOneSecondAfterInvalidHours_Expect_CancelledOrderState_And_ThrowException() {
        endTimeCount = startTimeCount.plus(25, ChronoUnit.HOURS).plus(1, ChronoUnit.SECONDS);

        when(variableClock.instant())
                .thenReturn(startTimeCount)
                .thenReturn(endTimeCount);

        order.submit();

        assertThrows(OrderExpiredException.class, () -> order.confirm());
        assertEquals(order.getOrderState(), Order.State.CANCELLED);
    }

    @Test
    void when_ElapsedInvalidHours_Expect_CancelledOrderState_And_ThrowException() {
        endTimeCount = startTimeCount.plus(UNVALID_PERIOD_HOURS, ChronoUnit.HOURS);

        when(variableClock.instant())
                .thenReturn(startTimeCount)
                .thenReturn(endTimeCount);

        order.submit();

        assertThrows(OrderExpiredException.class, () -> order.confirm());
        assertEquals(order.getOrderState(), Order.State.CANCELLED);
    }

}

package com.sprunck.druid.riemann;

import com.aphyr.riemann.client.EventDSL;
import com.aphyr.riemann.client.RiemannClient;
import com.google.common.base.Function;
import com.google.common.base.Supplier;
import com.google.common.base.Throwables;
import com.google.common.collect.Maps;
import com.metamx.common.ISE;
import com.metamx.common.guava.DefaultingHashMap;
import com.metamx.common.lifecycle.LifecycleStart;
import com.metamx.common.lifecycle.LifecycleStop;
import com.metamx.emitter.core.Emitter;
import com.metamx.emitter.core.Event;
import com.metamx.emitter.service.ServiceEvent;
import com.metamx.emitter.service.ServiceMetricEvent;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.atomic.AtomicBoolean;


/**
 * Sends Druid metrics to a riemann server.
 * The Riemann service attribute is composed by the druid service name and the metric name.
 * For instance, the event cache/total/hits 1 sent by a historical gives the following Riemann event:
 * <ul>
 * <li>Service : druid/historical cache/total/hits</li>
 * <li>Metric : 1</li>
 * </ul>
 */
public class RiemannEmitter implements Emitter {
    /**
     * The riemann client to send events.
     */
    private RiemannClient client;
    /**
     * Connection configuration.
     */
    private final RiemannEmitterConfig config;
    /**
     * Riemann event builder map indexed by event feed name.
     */
    private final static DefaultingHashMap<String, EventDSLBuilder> BUILDERS = new DefaultingHashMap<String, EventDSLBuilder>(
            new Supplier<EventDSLBuilder>() {
                @Override
                public EventDSLBuilder get() {
                    return new DefaultEventDSLBuilder();
                }
            }
    );

    /* Builder map initialization. */
    static {
        BUILDERS.put("metrics", new MetricsEventDSLBuilder());
    }

    /**
     * True if the emitter has been started, false otherwise.
     */
    private final AtomicBoolean started = new AtomicBoolean(false);

    /**
     * Constructor.
     *
     * @param config Configuration for the connection to the Riemann server.
     */
    public RiemannEmitter(RiemannEmitterConfig config) {
        this.config = config;
    }

    @Override
    @LifecycleStart
    public void start() {
        final boolean alreadyStarted = started.getAndSet(true);
        if (!alreadyStarted) {
            try {
                this.client = RiemannClient.tcp(config.getHost(), config.getPort());
                this.client.connect();
            } catch (IOException io) {
                throw Throwables.propagate(io);
            }
        }
    }

    @Override
    public void emit(Event event) {
        synchronized (started) {
            if (!started.get()) {
                throw new RejectedExecutionException("Service not started.");
            }
        }
        try {
            BUILDERS.get(event.getFeed())
                    .build(event, client)
                    .send()
                    .deref(5000, java.util.concurrent.TimeUnit.MILLISECONDS);
        } catch (IOException io) {
            throw Throwables.propagate(io);
        }
    }

    @Override
    public void flush() throws IOException {
    }

    @Override
    @LifecycleStop
    public void close() throws IOException {
        synchronized (started) {
            started.set(false);
            this.client.close();
        }
    }

    /**
     * Helper method that converts a druid event to a map.
     *
     * @param event A druid event.
     * @return a map containing all event attributes.
     */
    public static Map<String, String> event2Attr(Event event) {
        return Maps.transformValues(
                event.toMap(), new Function<Object, String>() {
                    @Override
                    public String apply(Object o) {
                        return o.toString();
                    }
                }
        );
    }

    /**
     * Interface for riemann event builders.
     */
    public interface EventDSLBuilder {
        /**
         * Builds Riemann event from a druid event.
         *
         * @param event  A druid event.
         * @param client A riemann client.
         * @return a Riemann event.
         */
        EventDSL build(final Event event, final RiemannClient client);
    }

    /**
     * Default builder for generic druid event.
     */
    private static class DefaultEventDSLBuilder implements EventDSLBuilder {
        @Override
        public EventDSL build(final Event event, final RiemannClient client) {
            ServiceEvent serviceEvent = (ServiceEvent) event;
            return client.event()
                    .host(serviceEvent.getHost())
                    .service(serviceEvent.getService())
                    .time(serviceEvent.getCreatedTime().getMillis() / 1000)
                    .attributes(event2Attr(event));
        }
    }

    /**
     * Builder for metrics.
     */
    private static class MetricsEventDSLBuilder implements EventDSLBuilder {
        @Override
        public EventDSL build(final Event event, final RiemannClient client) {
            ServiceMetricEvent metricEvent = (ServiceMetricEvent) event;
            String riemannService = metricEvent.getService() + ' ' + metricEvent.getMetric();
            EventDSL dsl = client.event()
                    .host(metricEvent.getHost())
                    .service(riemannService)
                    .time(metricEvent.getCreatedTime().getMillis() / 1000);

            // Evaluate if the metric value should be send as a double or an integer
            try {
                Double metric = metricEvent.getValue().doubleValue();
                if (metric == Math.ceil(metric)) {
                    dsl.metric(metric.longValue());
                } else {
                    dsl.metric(metric);
                }
            } catch (NumberFormatException e) {
                throw new ISE(e, "Bad metric format: %s", metricEvent.getMetric());
            }
            return dsl;
        }
    }
}

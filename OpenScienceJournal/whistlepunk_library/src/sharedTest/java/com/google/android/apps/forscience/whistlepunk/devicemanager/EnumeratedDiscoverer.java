/*
 *  Copyright 2016 Google Inc. All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package com.google.android.apps.forscience.whistlepunk.devicemanager;

import android.support.annotation.NonNull;

import com.google.android.apps.forscience.javalib.FailureListener;
import com.google.android.apps.forscience.whistlepunk.ExternalSensorProvider;
import com.google.android.apps.forscience.whistlepunk.data.GoosciSensorSpec;
import com.google.android.apps.forscience.whistlepunk.metadata.ExternalSensorSpec;

import org.junit.Assert;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * For testing, an {@link SensorDiscoverer} that knows exactly which specs it knows how to
 * return.
 */
public class EnumeratedDiscoverer extends StubSensorDiscoverer {
    private final List<ExternalSensorSpec> mSpecs = new ArrayList<>();

    public EnumeratedDiscoverer(ExternalSensorSpec... specs) {
        mSpecs.addAll(Arrays.asList(specs));
    }

    public void addSpec(ExternalSensorSpec spec) {
        mSpecs.add(spec);
    }

    @Override
    public boolean startScanning(ScanListener listener, FailureListener onScanError) {
        for (ExternalSensorSpec spec : mSpecs) {
            listener.onSensorFound(getDiscovered(spec));
        }
        return true;
    }

    @NonNull
    private SensorDiscoverer.DiscoveredSensor getDiscovered(final ExternalSensorSpec spec) {
        return new SensorDiscoverer.DiscoveredSensor() {

            @Override
            public GoosciSensorSpec.SensorSpec getSensorSpec() {
                return spec.asGoosciSpec();
            }

            @Override
            public SettingsInterface getSettingsInterface() {
                return null;
            }

            @Override
            public boolean shouldReplaceStoredSensor(ConnectableSensor oldSensor) {
                return false;
            }
        };
    }

    /**
     * For testing, generate a provider map that has providers that know how to generate exactly
     * the given set of specs.
     */
    public static Map<String, ExternalSensorProvider> buildProviderMap(
            ExternalSensorSpec... specs) {
        EnumeratedDiscoverer discoverer = new EnumeratedDiscoverer(specs);
        Map<String, ExternalSensorProvider> providers = new HashMap<>();
        for (ExternalSensorSpec spec : specs) {
            providers.put(spec.getType(), discoverer.getProvider());
        }
        return providers;
    }

    public static Map<String, SensorDiscoverer> buildDiscovererMap(
            ExternalSensorSpec... specs) {
        EnumeratedDiscoverer discoverer = new EnumeratedDiscoverer(specs);
        Map<String, SensorDiscoverer> discoverers = new HashMap<>();
        for (ExternalSensorSpec spec : specs) {
            discoverers.put(spec.getType(), discoverer);
        }
        return discoverers;
    }

    @Override
    protected ExternalSensorSpec buildSensorSpec(String name, byte[] config) {
        for (ExternalSensorSpec spec : mSpecs) {
            if (spec.getName().equals(name)) {
                return spec;
            }
        }
        Assert.fail("Can't find " + name + " in " + Arrays.asList(mSpecs));
        return null;
    }

}

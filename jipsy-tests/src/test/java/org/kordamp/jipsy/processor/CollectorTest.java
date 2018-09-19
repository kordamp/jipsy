/*

Copyright 2008-2018 TOPdesk, the Netherlands

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.

*/

package org.kordamp.jipsy.processor;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.kordamp.jipsy.processor.service.ServiceCollector;
import org.kordamp.jipsy.processor.service.Service;
import org.kordamp.jipsy.processor.testutils.NoOutputTestBase;
import org.kordamp.jipsy.processor.testutils.TestInitializer;
import org.kordamp.jipsy.processor.testutils.TestLogger;

import java.util.Collection;
import java.util.HashMap;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class CollectorTest extends NoOutputTestBase {

    private TestInitializer initializer;
    private TestLogger logger;
    private ServiceCollector collector;

    @Before
    public void loadFrameWork() {
        HashMap<String, String> map = new HashMap<String, String>();
        map.put("service1", "provider1\n");
        map.put("service2", "provider1\nprovider2\n");
        map.put("service3", "provider1\nprovider3\n");
        initializer = new TestInitializer(map);
        logger = new TestLogger();
        collector = new ServiceCollector(initializer, logger);
    }

    @Test(expected = NullPointerException.class)
    public void testGetServiceNull() {
        collector.getService(null);
    }

    @Test
    public void testGetServiceExisting() {
        assertEquals(0, collector.services().size());
        collector.getService("service");
        assertEquals(1, collector.services().size());
        logger.reset();
        Service service = collector.getService("service");
        assertTrue(logger.records().isEmpty());
        assertEquals("service", service.getName());
        assertEquals(1, collector.services().size());
    }

    @Test
    public void testGetServiceNew() {
        assertEquals(0, collector.services().size());
        logger.reset();
        assertEquals("service", collector.getService("service").getName());
        assertEquals(1, logger.records().size());
        assertEquals(1, collector.services().size());
    }

    @Test
    public void testGetServiceNewWithInitializer() {
        assertEquals(0, collector.services().size());
        Service service = collector.getService("service1");
        assertEquals("service1", service.getName());
        assertTrue(service.contains("provider1"));
        assertEquals(1, collector.services().size());
    }

    @Test
    public void testGetServiceNewWithBiggerInitializer() {
        assertEquals(0, collector.services().size());
        Service service = collector.getService("service2");
        assertEquals("service2", service.getName());
        assertTrue(service.contains("provider1"));
        assertTrue(service.contains("provider2"));
        assertEquals(1, collector.services().size());
    }

    @Test
    public void testGetServiceNewWithInitializerContainingRemovedElement() {
        assertEquals(0, collector.services().size());
        collector.removeProvider("provider1");
        Service service = collector.getService("service1");
        Assert.assertFalse(service.contains("provider1"));
        assertEquals(1, collector.services().size());
    }

    @Test
    public void testServicesEmpty() {
        Collection<Service> services = collector.services();
        assertEquals(0, services.size());
    }

    @Test
    public void testServicesOne() {
        Service service = collector.getService("service");
        Collection<Service> services = collector.services();
        assertEquals(1, services.size());
        assertTrue(services.contains(service));
    }

    @Test
    public void testServicesMore() {
        Service service1 = collector.getService("service1");
        Service service2 = collector.getService("service2");
        Collection<Service> services = collector.services();
        assertEquals(2, services.size());
        assertTrue(services.contains(service1));
        assertTrue(services.contains(service2));
    }

    @Test
    public void testServicesDuplicate() {
        Service service1 = collector.getService("service1");
        Service service2 = collector.getService("service1");
        assertTrue(service1 == service2);
        Collection<Service> services = collector.services();
        assertEquals(1, services.size());
        assertTrue(services.contains(service1));
    }

    @Test(expected = NullPointerException.class)
    public void testRemoveProviderNull() {
        collector.removeProvider(null);
    }

    @Test
    public void testRemoveProviderWhenEmpty() {
        collector.removeProvider("provider1");
        assertEquals(1, logger.records().size());
        assertEquals("Removing provider1\n", logger.getFileContent());
    }

    @Test
    public void testRemoveProviderWhenInNotOneService() {
        collector.getService("service1");
        logger.reset();
        collector.removeProvider("provider2");
        assertEquals(1, logger.records().size());
    }

    @Test
    public void testRemoveProviderWhenInOneService() {
        collector.getService("service1");
        logger.reset();
        collector.removeProvider("provider1");
        assertEquals(2, logger.records().size());
    }

    @Test
    public void testRemoveProviderWhenInTwoServices() {
        collector.getService("service1");
        collector.getService("service2");
        logger.reset();
        collector.removeProvider("provider1");
        assertEquals(3, logger.records().size());
    }

    @Test
    public void testRemoveProviderWhenInSomeServices() {
        collector.getService("service1");
        collector.getService("service2");
        collector.getService("service3");
        logger.reset();
        collector.removeProvider("provider2");
        assertEquals(2, logger.records().size());
    }

    @Test
    public void testToStringEmpty() {
        collector.toString();
    }

    @Test
    public void testToStringNonExistingService() {
        collector.getService("nonExistingService");
        collector.toString();
    }

    @Test
    public void testToStringExistingService() {
        collector.getService("service1");
        collector.toString();
    }

    @Test
    public void testToStringMoreExistingServices() {
        collector.getService("service1");
        collector.getService("service2");
        collector.toString();
    }
}
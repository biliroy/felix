/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.felix.scr.integration;


import java.util.Collection;

import junit.framework.Assert;
import junit.framework.TestCase;

import org.apache.felix.scr.Component;
import org.apache.felix.scr.integration.components.SimpleComponent;
import org.apache.felix.scr.integration.components.SimpleServiceImpl;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.ops4j.pax.exam.junit.JUnit4TestRunner;
import org.osgi.framework.Constants;
import org.osgi.service.component.runtime.dto.ComponentConfigurationDTO;


@RunWith(JUnit4TestRunner.class)
public class ComponentConfigurationTest extends ComponentTestBase
{
    static
    {
        // uncomment to enable debugging of this test class
//          paxRunnerVmOption = DEBUG_VM_OPTION;
    }


    @Test
    public void test_SimpleComponent_configuration_ignore()
    {
        final String pid = "SimpleComponent.configuration.ignore";
        TestCase.assertNull( SimpleComponent.INSTANCE );

        deleteConfig( pid );
        delay();

        ComponentConfigurationDTO cc = getDisabledConfigurationAndEnable(pid, ComponentConfigurationDTO.ACTIVE);
        
        TestCase.assertNotNull( SimpleComponent.INSTANCE );
        TestCase.assertNull( SimpleComponent.INSTANCE.getProperty( PROP_NAME ) );

        configure( pid );
        delay();

        findComponentConfigurationByName(pid, ComponentConfigurationDTO.ACTIVE);
        TestCase.assertNotNull( SimpleComponent.INSTANCE );
        TestCase.assertNull( SimpleComponent.INSTANCE.getProperty( PROP_NAME ) );

        deleteConfig( pid );
        delay();

        findComponentConfigurationByName(pid, ComponentConfigurationDTO.ACTIVE);
        TestCase.assertNotNull( SimpleComponent.INSTANCE );
        TestCase.assertNull( SimpleComponent.INSTANCE.getProperty( PROP_NAME ) );

        disableAndCheck( cc );
        TestCase.assertNull( SimpleComponent.INSTANCE );
    }


    @Test
    public void test_SimpleComponent_configuration_optional()
    {
        final String pid = "SimpleComponent.configuration.optional";
        ComponentConfigurationDTO cc = getDisabledConfigurationAndEnable(pid, ComponentConfigurationDTO.ACTIVE);

        final SimpleComponent firstInstance = SimpleComponent.INSTANCE;
        TestCase.assertNotNull( firstInstance );
        TestCase.assertNull( firstInstance.getProperty( PROP_NAME ) );

        configure( pid );
        delay();

        final SimpleComponent secondInstance = SimpleComponent.INSTANCE;
        findComponentConfigurationByName(pid, ComponentConfigurationDTO.ACTIVE);
        TestCase.assertNotNull( secondInstance );
        TestCase.assertEquals( PROP_NAME, secondInstance.getProperty( PROP_NAME ) );

        deleteConfig( pid );
        delay();

        final SimpleComponent thirdInstance = SimpleComponent.INSTANCE;
        findComponentConfigurationByName(pid, ComponentConfigurationDTO.ACTIVE);
        TestCase.assertNotNull( thirdInstance );
        TestCase.assertNull( thirdInstance.getProperty( PROP_NAME ) );

        TestCase.assertNotSame( "Expect new instance object after reconfiguration", firstInstance, secondInstance );
        TestCase.assertNotSame( "Expect new instance object after configuration deletion (1)", firstInstance,
            thirdInstance );
        TestCase.assertNotSame( "Expect new instance object after configuration deletion (2)", secondInstance,
            thirdInstance );

        disableAndCheck( cc );
        TestCase.assertNull( SimpleComponent.INSTANCE );
    }


    @Test
    public void test_SimpleComponent_configuration_require()
    {
        final String pid = "SimpleComponent.configuration.require";

        deleteConfig( pid );
        delay();
        
        TestCase.assertNull( SimpleComponent.INSTANCE );

        getConfigurationsDisabledThenEnable(pid, 0, ComponentConfigurationDTO.UNSATISFIED);
        TestCase.assertNull( SimpleComponent.INSTANCE );

        configure( pid );
        delay();

        ComponentConfigurationDTO cc = findComponentConfigurationByName(pid, ComponentConfigurationDTO.ACTIVE);
        TestCase.assertNotNull( SimpleComponent.INSTANCE );
        TestCase.assertEquals( PROP_NAME, SimpleComponent.INSTANCE.getProperty( PROP_NAME ) );

        deleteConfig( pid );
        delay();

        checkConfigurationCount(pid, 0, -1);
        TestCase.assertNull( SimpleComponent.INSTANCE );

        disableAndCheck( cc );
        TestCase.assertNull( SimpleComponent.INSTANCE );
    }


    @Test
    public void test_SimpleComponent_dynamic_configuration()
    {
        final String pid = "DynamicConfigurationComponent";
        boolean pre13 = true;
        dynamicConfigTest(pid, pre13);
    }

    @Test
    public void test_SimpleComponent_dynamic_configuration_13()
    {
        final String pid = "DynamicConfigurationComponent13";
        boolean pre13 = false;
        dynamicConfigTest(pid, pre13);
    }
    
    @Test
    public void test_SimpleComponent_dynamic_configuration_flag()
    {
        final String pid = "DynamicConfigurationComponentFlag";
        boolean pre13 = false;
        dynamicConfigTest(pid, pre13);
    }


	private void dynamicConfigTest(final String pid, boolean pre13) {
        deleteConfig( pid );
        delay();

        ComponentConfigurationDTO cc = getDisabledConfigurationAndEnable(pid, ComponentConfigurationDTO.ACTIVE);

        TestCase.assertNotNull( SimpleComponent.INSTANCE );
        TestCase.assertNull( SimpleComponent.INSTANCE.getProperty( PROP_NAME ) );
        TestCase.assertEquals( pid, SimpleComponent.INSTANCE.getProperty( Constants.SERVICE_PID ) );

        final SimpleComponent instance = SimpleComponent.INSTANCE;

        configure( pid );
        delay();

        findComponentConfigurationByName(pid, ComponentConfigurationDTO.ACTIVE);
        TestCase.assertEquals( instance, SimpleComponent.INSTANCE );
        TestCase.assertEquals( PROP_NAME, SimpleComponent.INSTANCE.getProperty( PROP_NAME ) );
        TestCase.assertEquals( pid, SimpleComponent.INSTANCE.getProperty( Constants.SERVICE_PID ) );

        deleteConfig( pid );
        delay();

        findComponentConfigurationByName(pid, ComponentConfigurationDTO.ACTIVE);
        if (pre13)
        {
            TestCase.assertNotSame( instance, SimpleComponent.INSTANCE );
        }
        else
        {
            TestCase.assertSame( instance, SimpleComponent.INSTANCE );
        }
        TestCase.assertNull( SimpleComponent.INSTANCE.getProperty( PROP_NAME ) );
        TestCase.assertEquals( pid, SimpleComponent.INSTANCE.getProperty( Constants.SERVICE_PID ) );

        disableAndCheck( cc );
        TestCase.assertNull( SimpleComponent.INSTANCE );
	}


    @Test
    public void test_SimpleComponent_dynamic_optional_configuration_with_required_service()
    {
        final String targetProp = "ref.target";
        final String filterProp = "required";
        final SimpleServiceImpl service = SimpleServiceImpl.create( bundleContext, "sample" ).setFilterProperty( filterProp );
        try
        {
            final String pid = "DynamicConfigurationComponentWithRequiredReference";
            deleteConfig( pid );
            delay();

            // mandatory ref missing --> component unsatisfied
            ComponentConfigurationDTO cc = getDisabledConfigurationAndEnable(pid, ComponentConfigurationDTO.UNSATISFIED);

            // dynamically configure without the correct target
            configure( pid );
            delay();

            // mandatory ref missing --> component unsatisfied
            findComponentConfigurationByName(pid, ComponentConfigurationDTO.UNSATISFIED);

            // dynamically configure with correct target
            theConfig.put( targetProp, "(filterprop=" + filterProp + ")" );
            configure( pid );
            delay();

            findComponentConfigurationByName(pid, ComponentConfigurationDTO.ACTIVE);
            TestCase.assertNotNull( SimpleComponent.INSTANCE );
            TestCase.assertEquals( PROP_NAME, SimpleComponent.INSTANCE.getProperty( PROP_NAME ) );
            TestCase.assertEquals( pid, SimpleComponent.INSTANCE.getProperty( Constants.SERVICE_PID ) );

            final SimpleComponent instance = SimpleComponent.INSTANCE;

            configure( pid );
            delay();

            // same instance after reconfiguration
            findComponentConfigurationByName(pid, ComponentConfigurationDTO.ACTIVE);
            TestCase.assertEquals( instance, SimpleComponent.INSTANCE );
            TestCase.assertEquals( PROP_NAME, SimpleComponent.INSTANCE.getProperty( PROP_NAME ) );
            TestCase.assertEquals( pid, SimpleComponent.INSTANCE.getProperty( Constants.SERVICE_PID ) );
            TestCase.assertNotNull( SimpleComponent.INSTANCE.m_singleRef );

            // reconfigure without target --> unsatisifed
            theConfig.remove( targetProp );
            configure( pid );
            delay();

            // mandatory ref missing --> component unsatisfied
            findComponentConfigurationByName(pid, ComponentConfigurationDTO.UNSATISFIED);

            deleteConfig( pid );
            delay();

            // mandatory ref missing --> component unsatisfied
            findComponentConfigurationByName(pid, ComponentConfigurationDTO.UNSATISFIED);

            disableAndCheck(cc);
            TestCase.assertNull( SimpleComponent.INSTANCE );
        }
        finally
        {
            theConfig.remove( targetProp );
            if ( service != null )
            {
                service.drop();
            }
        }
    }

    /**
     * FELIX-3902.  Start with filter matching two services, remove one, then change the filter
     * to (still) match the other one.  2nd service should remain bound.
     */
    @Test
    public void test_SimpleComponent_dynamic_optional_configuration_with_required_service2()
    {
        final String targetProp = "ref.target";
        final String filterProp1 = "one";
        final String filterProp2 = "two";
        final SimpleServiceImpl service1 = SimpleServiceImpl.create( bundleContext, "one", 1 ).setFilterProperty( filterProp1 );
        final SimpleServiceImpl service2 = SimpleServiceImpl.create( bundleContext, "two", 2 ).setFilterProperty( filterProp2 );
        try
        {
            final String pid = "DynamicConfigurationComponentWithRequiredReference";
            deleteConfig( pid );
            delay();

            // mandatory ref missing --> component unsatisfied
            ComponentConfigurationDTO cc = getDisabledConfigurationAndEnable(pid, ComponentConfigurationDTO.UNSATISFIED);

            // dynamically configure without the correct target
            configure( pid );
            delay();

            // mandatory ref missing --> component unsatisfied
            findComponentConfigurationByName(pid, ComponentConfigurationDTO.UNSATISFIED);

            // dynamically configure with correct target
            theConfig.put( targetProp, "(|(filterprop=" + filterProp1 + ")(filterprop=" + filterProp2 + "))" );
            configure( pid );
            delay();

            findComponentConfigurationByName(pid, ComponentConfigurationDTO.ACTIVE);
            TestCase.assertNotNull( SimpleComponent.INSTANCE );
            TestCase.assertEquals( PROP_NAME, SimpleComponent.INSTANCE.getProperty( PROP_NAME ) );
            TestCase.assertEquals( pid, SimpleComponent.INSTANCE.getProperty( Constants.SERVICE_PID ) );

            final SimpleComponent instance = SimpleComponent.INSTANCE;

            configure( pid );
            delay();

            //remove higher ranked service
            if (service2 != null)
            {
                service2.drop();
            }
             // same instance after reconfiguration
            findComponentConfigurationByName(pid, ComponentConfigurationDTO.ACTIVE);
            TestCase.assertEquals( instance, SimpleComponent.INSTANCE );
            TestCase.assertEquals( PROP_NAME, SimpleComponent.INSTANCE.getProperty( PROP_NAME ) );
            TestCase.assertEquals( pid, SimpleComponent.INSTANCE.getProperty( Constants.SERVICE_PID ) );
            TestCase.assertNotNull( SimpleComponent.INSTANCE.m_singleRef );

            // reconfigure with new filter --> active
            theConfig.put( targetProp, "(filterprop=" + filterProp1 + ")" );
            configure( pid );
            delay();

            // same instance after reconfiguration
            findComponentConfigurationByName(pid, ComponentConfigurationDTO.ACTIVE);
            TestCase.assertEquals( instance, SimpleComponent.INSTANCE );
            TestCase.assertEquals( PROP_NAME, SimpleComponent.INSTANCE.getProperty( PROP_NAME ) );
            TestCase.assertEquals( pid, SimpleComponent.INSTANCE.getProperty( Constants.SERVICE_PID ) );
            TestCase.assertNotNull( SimpleComponent.INSTANCE.m_singleRef );

            deleteConfig( pid );
            delay();

            // mandatory ref missing --> component unsatisfied
            findComponentConfigurationByName(pid, ComponentConfigurationDTO.UNSATISFIED);

            disableAndCheck(cc);
            TestCase.assertNull( SimpleComponent.INSTANCE );
        }
        finally
        {
            theConfig.remove( targetProp );
            if ( service1 != null )
            {
                service1.drop();
            }
        }
    }

    @Test
    public void test_SimpleComponent_dynamic_optional_configuration_with_optional_service() throws Exception
    {
        final String targetProp = "ref.target";
        final String filterProp = "required";
        final SimpleServiceImpl service = SimpleServiceImpl.create( bundleContext, "sample" ).setFilterProperty( filterProp );
        try
        {
            final String pid = "DynamicConfigurationComponentWithOptionalReference";
            deleteConfig( pid );
            delay();

            // optional ref missing --> component active
            ComponentConfigurationDTO cc = getDisabledConfigurationAndEnable(pid, ComponentConfigurationDTO.ACTIVE);

            TestCase.assertNotNull( SimpleComponent.INSTANCE );
            final SimpleComponent instance = SimpleComponent.INSTANCE;

            // dynamically configure without the correct target
            configure( pid );
            delay();

            // optional ref missing --> component active
            findComponentConfigurationByName(pid, ComponentConfigurationDTO.ACTIVE);
            TestCase.assertEquals( instance, SimpleComponent.INSTANCE );
            TestCase.assertNull( SimpleComponent.INSTANCE.m_singleRef );

            // dynamically configure with correct target
            theConfig.put( targetProp, "(filterprop=" + filterProp + ")" );
            configure( pid );
            delay();

            findComponentConfigurationByName(pid, ComponentConfigurationDTO.ACTIVE);
            TestCase.assertEquals( instance, SimpleComponent.INSTANCE );
            TestCase.assertEquals( PROP_NAME, SimpleComponent.INSTANCE.getProperty( PROP_NAME ) );
            TestCase.assertEquals( pid, SimpleComponent.INSTANCE.getProperty( Constants.SERVICE_PID ) );
            TestCase.assertNotNull( SimpleComponent.INSTANCE.m_singleRef );

            configure( pid );
            delay();

            // same instance after reconfiguration
            findComponentConfigurationByName(pid, ComponentConfigurationDTO.ACTIVE);
            TestCase.assertEquals( instance, SimpleComponent.INSTANCE );
            TestCase.assertEquals( PROP_NAME, SimpleComponent.INSTANCE.getProperty( PROP_NAME ) );
            TestCase.assertEquals( pid, SimpleComponent.INSTANCE.getProperty( Constants.SERVICE_PID ) );
            TestCase.assertNotNull( SimpleComponent.INSTANCE.m_singleRef );

            // reconfigure without target --> active
            theConfig.remove( targetProp );
            configure( pid );
            delay();

            // optional ref missing --> component active
            findComponentConfigurationByName(pid, ComponentConfigurationDTO.ACTIVE);
            TestCase.assertEquals( instance, SimpleComponent.INSTANCE );
            TestCase.assertNull( SimpleComponent.INSTANCE.m_singleRef );

            deleteConfig( pid );
            delay();

            // optional ref missing --> component active
            findComponentConfigurationByName(pid, ComponentConfigurationDTO.ACTIVE);
            TestCase.assertNotSame( instance, SimpleComponent.INSTANCE );
            TestCase.assertNull( SimpleComponent.INSTANCE.m_singleRef );

            disableAndCheck(cc);
            TestCase.assertNull( SimpleComponent.INSTANCE );
        }
        finally
        {
//            Thread.sleep( 60000 );
            theConfig.remove( targetProp );
            if ( service != null )
            {
                service.drop();
            }
        }
    }


    @Test
    public void test_SimpleComponent_factory_configuration()
    {
        final String factoryPid = "FactoryConfigurationComponent";

        deleteFactoryConfigurations( factoryPid );
        delay();

        getConfigurationsDisabledThenEnable(factoryPid, 0, -1);
        TestCase.assertTrue( SimpleComponent.INSTANCES.isEmpty() );

        // create two factory configurations expecting two components
        final String pid0 = createFactoryConfiguration( factoryPid );
        final String pid1 = createFactoryConfiguration( factoryPid );
        delay();

        // expect two active components, //TODO WTF?? only first is active, second is disabled
        checkConfigurationCount(factoryPid, 2, ComponentConfigurationDTO.ACTIVE);
//        Collection<ComponentConfigurationDTO> ccs = findComponentConfigurationsByName(factoryPid);
//        Assert.assertEquals(2, ccs.size());
//        
//        ComponentConfigurationDTO activ e= null;
//        ComponentConfigurationDTO inactive = null;
//        
//        for (ComponentConfigurationDTO cc: ccs)
//        {
//        	if (ComponentConfigurationDTO.ACTIVE == cc.state)
//        	{
//        		if 
//        	}
//        }
//
//        // find the active and inactive configs, fail if none
//        int activeConfig;
//        int inactiveConfig;
//        if ( twoConfigs[0].getState() == Component.STATE_ACTIVE )
//        {
//            // [0] is active, [1] expected disabled
//            activeConfig = 0;
//            inactiveConfig = 1;
//        }
//        else if ( twoConfigs[1].getState() == Component.STATE_ACTIVE )
//        {
//            // [1] is active, [0] expected disabled
//            activeConfig = 1;
//            inactiveConfig = 0;
//        }
//        else
//        {
//            TestCase.fail( "One of two components expected active" );
//            return; // eases the compiler...
//        }
//
//        TestCase.assertEquals( Component.STATE_ACTIVE, twoConfigs[activeConfig].getState() );
//        TestCase.assertEquals( Component.STATE_DISABLED, twoConfigs[inactiveConfig].getState() );
//        TestCase.assertEquals( 1, SimpleComponent.INSTANCES.size() );
//        TestCase.assertTrue( SimpleComponent.INSTANCES.containsKey( twoConfigs[activeConfig].getId() ) );
//        TestCase.assertFalse( SimpleComponent.INSTANCES.containsKey( twoConfigs[inactiveConfig].getId() ) );
//
//        // enable second component
//        twoConfigs[inactiveConfig].enable();
//        delay();
//
//        // ensure both components active
//        TestCase.assertEquals( Component.STATE_ACTIVE, twoConfigs[0].getState() );
//        TestCase.assertEquals( Component.STATE_ACTIVE, twoConfigs[1].getState() );
//        TestCase.assertEquals( 2, SimpleComponent.INSTANCES.size() );
//        TestCase.assertTrue( SimpleComponent.INSTANCES.containsKey( twoConfigs[0].getId() ) );
//        TestCase.assertTrue( SimpleComponent.INSTANCES.containsKey( twoConfigs[1].getId() ) );

        // delete a configuration
        deleteConfig( pid0 );
        delay();

        // expect one component
        checkConfigurationCount(factoryPid, 1, ComponentConfigurationDTO.ACTIVE);
//        final Component[] oneConfig = findComponentConfigurationsByName( factoryPid );
//        TestCase.assertNotNull( oneConfig );
//        TestCase.assertEquals( 1, oneConfig.length );
//        TestCase.assertEquals( Component.STATE_ACTIVE, oneConfig[0].getState() );
//        TestCase.assertEquals( 1, SimpleComponent.INSTANCES.size() );
//        TestCase.assertTrue( SimpleComponent.INSTANCES.containsKey( oneConfig[0].getId() ) );

        // delete second configuration
        deleteConfig( pid1 );
        delay();

        checkConfigurationCount(factoryPid, 0, ComponentConfigurationDTO.ACTIVE);
        // expect a single unsatisfied component
//        final Component[] configsDeleted = findComponentConfigurationsByName( factoryPid );
//        TestCase.assertNotNull( configsDeleted );
//        TestCase.assertEquals( 1, configsDeleted.length );
//        TestCase.assertEquals( Component.STATE_UNSATISFIED, configsDeleted[0].getState() );
//        TestCase.assertEquals( 0, SimpleComponent.INSTANCES.size() );
//        disableAndCheck(cd); //TODO needed??
    }

    @Test
    public void test_SimpleComponent_factory_configuration_enabled()
    {
        final String factoryPid = "FactoryConfigurationComponent_enabled";

        deleteFactoryConfigurations( factoryPid );
        delay();

        checkConfigurationCount(factoryPid, 0, ComponentConfigurationDTO.ACTIVE);
        // no component config exists without configuration

        // create two factory configurations expecting two components
        final String pid0 = createFactoryConfiguration( factoryPid );
        final String pid1 = createFactoryConfiguration( factoryPid );
        delay();

        // expect two components, all active
        checkConfigurationCount(factoryPid, 2, ComponentConfigurationDTO.ACTIVE);

        // disable the name component
        disableAndCheck( factoryPid );
        delay();

        // expect no XXtwo disabled components
//        checkConfigurationCount(factoryPid, 0, ComponentConfigurationDTO.ACTIVE);
//        final Component[] twoConfigsDisabled = findComponentConfigurationsByName( factoryPid );
//        TestCase.assertNotNull( twoConfigsDisabled );
//        TestCase.assertEquals( 2, twoConfigsDisabled.length );
//        TestCase.assertEquals( Component.STATE_DISABLED, twoConfigsDisabled[0].getState() );
//        TestCase.assertEquals( Component.STATE_DISABLED, twoConfigsDisabled[1].getState() );
//        TestCase.assertEquals( 0, SimpleComponent.INSTANCES.size() );
//        TestCase.assertFalse( SimpleComponent.INSTANCES.containsKey( twoConfigs[0].getId() ) );
//        TestCase.assertFalse( SimpleComponent.INSTANCES.containsKey( twoConfigs[1].getId() ) );

        // create a configuration
        final String pid3 = createFactoryConfiguration( factoryPid );
        delay();

        // expect three disabled components
//        checkConfigurationCount(factoryPid, 0, ComponentConfigurationDTO.ACTIVE);
//        final Component[] threeConfigsDisabled = findComponentConfigurationsByName( factoryPid );
//        TestCase.assertNotNull( threeConfigsDisabled );
//        TestCase.assertEquals( 3, threeConfigsDisabled.length );
//        TestCase.assertEquals( Component.STATE_DISABLED, threeConfigsDisabled[0].getState() );
//        TestCase.assertEquals( Component.STATE_DISABLED, threeConfigsDisabled[1].getState() );
//        TestCase.assertEquals( Component.STATE_DISABLED, threeConfigsDisabled[2].getState() );
//        TestCase.assertEquals( 0, SimpleComponent.INSTANCES.size() );
//        TestCase.assertFalse( SimpleComponent.INSTANCES.containsKey( threeConfigsDisabled[0].getId() ) );
//        TestCase.assertFalse( SimpleComponent.INSTANCES.containsKey( threeConfigsDisabled[1].getId() ) );
//        TestCase.assertFalse( SimpleComponent.INSTANCES.containsKey( threeConfigsDisabled[2].getId() ) );
//disabled, no configurations.
        getConfigurationsDisabledThenEnable(factoryPid, 0, ComponentConfigurationDTO.ACTIVE);
        
//        // enable a single component (to get ComponentContext later)
//        threeConfigsDisabled[0].enable();
//        delay();
//
//        // expect one enabled and two disabled components
//        final Component[] threeConfigs21 = findComponentConfigurationsByName( factoryPid );
//        TestCase.assertNotNull( threeConfigs21 );
//        TestCase.assertEquals( 3, threeConfigs21.length );
//        TestCase.assertEquals( 1, SimpleComponent.INSTANCES.size() );
//        TestCase.assertEquals( Component.STATE_ACTIVE, threeConfigs21[0].getState() );
//        TestCase.assertEquals( Component.STATE_DISABLED, threeConfigs21[1].getState() );
//        TestCase.assertEquals( Component.STATE_DISABLED, threeConfigs21[2].getState() );
//        TestCase.assertTrue( SimpleComponent.INSTANCES.containsKey( threeConfigs21[0].getId() ) );
//        TestCase.assertFalse( SimpleComponent.INSTANCES.containsKey( threeConfigs21[1].getId() ) );
//        TestCase.assertFalse( SimpleComponent.INSTANCES.containsKey( threeConfigs21[2].getId() ) );
//
//        // enable all components now
//        SimpleComponent.INSTANCES.values().iterator().next().m_activateContext.enableComponent( factoryPid );
//        delay();
//
//        // expect all enabled
//        final Component[] threeConfigsEnabled = findComponentConfigurationsByName( factoryPid );
//        TestCase.assertNotNull( threeConfigsEnabled );
//        TestCase.assertEquals( 3, threeConfigsEnabled.length );
//        TestCase.assertEquals( Component.STATE_ACTIVE, threeConfigsEnabled[0].getState() );
//        TestCase.assertEquals( Component.STATE_ACTIVE, threeConfigsEnabled[1].getState() );
//        TestCase.assertEquals( Component.STATE_ACTIVE, threeConfigsEnabled[2].getState() );
//        TestCase.assertEquals( 3, SimpleComponent.INSTANCES.size() );
//        TestCase.assertTrue( SimpleComponent.INSTANCES.containsKey( threeConfigsEnabled[0].getId() ) );
//        TestCase.assertTrue( SimpleComponent.INSTANCES.containsKey( threeConfigsEnabled[1].getId() ) );
//        TestCase.assertTrue( SimpleComponent.INSTANCES.containsKey( threeConfigsEnabled[2].getId() ) );
    }


}

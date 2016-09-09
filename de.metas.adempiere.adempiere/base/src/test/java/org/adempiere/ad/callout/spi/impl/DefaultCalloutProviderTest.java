package org.adempiere.ad.callout.spi.impl;

/*
 * #%L
 * de.metas.adempiere.adempiere.base
 * %%
 * Copyright (C) 2015 metas GmbH
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 2 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-2.0.html>.
 * #L%
 */

import java.util.List;
import java.util.Properties;

import org.adempiere.ad.callout.api.ICalloutField;
import org.adempiere.ad.callout.api.ICalloutInstance;
import org.adempiere.ad.callout.api.TableCalloutsMap;
import org.adempiere.ad.callout.api.impl.MethodNameCalloutInstance;
import org.adempiere.ad.callout.api.impl.MockedCalloutField;
import org.adempiere.ad.callout.exceptions.CalloutInitException;
import org.adempiere.ad.trx.api.ITrx;
import org.adempiere.model.InterfaceWrapperHelper;
import org.adempiere.test.AdempiereTestHelper;
import org.adempiere.test.AdempiereTestWatcher;
import org.compiere.model.CalloutEngine;
import org.compiere.model.GridField;
import org.compiere.model.GridTab;
import org.compiere.model.I_AD_ColumnCallout;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

public class DefaultCalloutProviderTest
{
	public static abstract class AbstractMockedCallout extends CalloutEngine
	{
		public AbstractMockedCallout()
		{
			super();
		}

		public AbstractMockedCallout(final boolean throwInitExceptionOnCtor)
		{
			this();
			if (throwInitExceptionOnCtor)
			{
				throw new CalloutInitException("Programatic init exception");
			}
		}

		public static final String METHOD_method1 = "method1";
		public String method1(final ICalloutField calloutField)
		{
			return "";
		}
		
		public static final String METHOD_method2 = "method2";
		public String method2(Properties ctx, int WindowNo, GridTab mTab, GridField mField, Object value)
		{
			return "";
		}

		public static final String METHOD_method3 = "method3";
		public String method3(Properties ctx, int WindowNo, GridTab mTab, GridField mField, Object value, Object valueOld)
		{
			return "";
		}

	}

	public static class Callout1 extends AbstractMockedCallout
	{
	};

	public static class Callout2 extends AbstractMockedCallout
	{
	};

	public static class Callout3 extends AbstractMockedCallout
	{
	};

	public static class Callout4 extends AbstractMockedCallout
	{
	}

	public static class Callout_FailOnInit extends AbstractMockedCallout
	{
		public Callout_FailOnInit()
		{
			super(true);
		}
	}
	
	@Rule
	public final AdempiereTestWatcher testWatcher = new AdempiereTestWatcher();

	private DefaultCalloutProvider calloutsProvider;

	@Before
	public void init()
	{
		AdempiereTestHelper.get().init();
		calloutsProvider = new DefaultCalloutProvider();
	}

	@Test
	public void test_StandardCase()
	{
		final MockedCalloutField field = MockedCalloutField.createNewField();

		createAD_ColumnCallout(field, Callout1.class, Callout1.METHOD_method1);
		createAD_ColumnCallout(field, Callout1.class, Callout1.METHOD_method2);
		createAD_ColumnCallout(field, Callout1.class, Callout1.METHOD_method3);
		//
		createAD_ColumnCallout(field, Callout2.class, Callout2.METHOD_method1);
		createAD_ColumnCallout(field, Callout3.class, Callout3.METHOD_method1);
		createAD_ColumnCallout(field, Callout4.class, Callout4.METHOD_method1);

		final TableCalloutsMap tableCallouts = calloutsProvider.getCallouts(field.getCtx(), field.getAD_Table_ID());
		final List<ICalloutInstance> calloutInstances = tableCallouts.getColumnCallouts(field.getAD_Column_ID());
		Assert.assertEquals("Invalid callouts list size: " + calloutInstances, 6, calloutInstances.size());

		assertLegacyCalloutInstance(calloutInstances.get(0), Callout1.class, Callout1.METHOD_method1);
		assertLegacyCalloutInstance(calloutInstances.get(1), Callout1.class, Callout1.METHOD_method2);
		assertLegacyCalloutInstance(calloutInstances.get(2), Callout1.class, Callout1.METHOD_method3);
		//
		assertLegacyCalloutInstance(calloutInstances.get(3), Callout2.class, Callout2.METHOD_method1);
		assertLegacyCalloutInstance(calloutInstances.get(4), Callout3.class, Callout3.METHOD_method1);
		assertLegacyCalloutInstance(calloutInstances.get(5), Callout4.class, Callout4.METHOD_method1);
	}

	@Test
	public void test_NoCallouts()
	{
		final MockedCalloutField field = MockedCalloutField.createNewField();

		final TableCalloutsMap tableCallouts = calloutsProvider.getCallouts(field.getCtx(), field.getAD_Table_ID());
		;
		final List<ICalloutInstance> calloutInstances = tableCallouts.getColumnCallouts(field.getAD_Column_ID());
		Assert.assertEquals("Invalid callouts list size: " + calloutInstances, 0, calloutInstances.size());
	}

	@Test
	public void test_OnlyInactiveCallouts()
	{
		final MockedCalloutField field = MockedCalloutField.createNewField();

		final I_AD_ColumnCallout cc1 = createAD_ColumnCallout(field, Callout1.class, Callout1.METHOD_method1);
		cc1.setIsActive(false);
		InterfaceWrapperHelper.save(cc1);

		final TableCalloutsMap tableCallouts = calloutsProvider.getCallouts(field.getCtx(), field.getAD_Table_ID());
		final List<ICalloutInstance> calloutInstances = tableCallouts.getColumnCallouts(field.getAD_Column_ID());
		Assert.assertEquals("Invalid callouts list size: " + calloutInstances, 0, calloutInstances.size());
	}

	@Test
	public void test_StandardCase_OneCalloutFailsOnInit()
	{
		final MockedCalloutField field = MockedCalloutField.createNewField();

		createAD_ColumnCallout(field, Callout1.class, Callout1.METHOD_method1);
		createAD_ColumnCallout(field, Callout2.class, Callout2.METHOD_method1);
		createAD_ColumnCallout(field, Callout_FailOnInit.class, Callout_FailOnInit.METHOD_method1);
		createAD_ColumnCallout(field, Callout4.class, Callout4.METHOD_method1);

		final TableCalloutsMap tableCallouts = calloutsProvider.getCallouts(field.getCtx(), field.getAD_Table_ID());
		final List<ICalloutInstance> calloutInstances = tableCallouts.getColumnCallouts(field.getAD_Column_ID());
		Assert.assertEquals("Invalid callouts list size: " + calloutInstances, 3, calloutInstances.size());

		assertLegacyCalloutInstance(calloutInstances.get(0), Callout1.class, Callout1.METHOD_method1);
		assertLegacyCalloutInstance(calloutInstances.get(1), Callout2.class, Callout2.METHOD_method1);
		assertLegacyCalloutInstance(calloutInstances.get(2), Callout4.class, Callout4.METHOD_method1);
	}

	@Test
	public void test_DuplicateCallouts()
	{
		final MockedCalloutField field = MockedCalloutField.createNewField();

		createAD_ColumnCallout(field, Callout1.class, Callout1.METHOD_method1);
		createAD_ColumnCallout(field, Callout1.class, Callout1.METHOD_method1);
		createAD_ColumnCallout(field, Callout2.class, Callout3.METHOD_method1);

		final TableCalloutsMap tableCallouts = calloutsProvider.getCallouts(field.getCtx(), field.getAD_Table_ID());
		final List<ICalloutInstance> calloutInstances = tableCallouts.getColumnCallouts(field.getAD_Column_ID());
		Assert.assertEquals("Invalid callouts list size: " + calloutInstances, 2, calloutInstances.size());

		assertLegacyCalloutInstance(calloutInstances.get(0), Callout1.class, Callout1.METHOD_method1);
		assertLegacyCalloutInstance(calloutInstances.get(1), Callout2.class, Callout2.METHOD_method1);
	}

	private I_AD_ColumnCallout createAD_ColumnCallout(final ICalloutField field, final Class<?> calloutClass, final String methodName)
	{
		final String calloutClassName = calloutClass.getName() + "." + methodName;
		return createAD_ColumnCallout(field, calloutClassName);
	}

	private I_AD_ColumnCallout createAD_ColumnCallout(final ICalloutField field, final String calloutClassName)
	{
		final int AD_Column_ID = field.getAD_Column_ID();
		Assert.assertTrue("AD_Column_ID is set for " + field, AD_Column_ID > 0);

		final I_AD_ColumnCallout cc = InterfaceWrapperHelper.create(field.getCtx(), I_AD_ColumnCallout.class, ITrx.TRXNAME_None);
		cc.setAD_Column_ID(AD_Column_ID);
		cc.setClassname(calloutClassName);
		cc.setSeqNo(0);
		cc.setIsActive(true);
		InterfaceWrapperHelper.save(cc);
		return cc;
	}

	private void assertLegacyCalloutInstance(final ICalloutInstance calloutInstance, final Class<?> calloutClass, final String methodName)
	{
		Assert.assertNotNull("calloutInstance not null", calloutInstance);
		Assert.assertTrue("calloutInstance is not instanceof " + MethodNameCalloutInstance.class + ": " + calloutInstance,
				calloutInstance instanceof MethodNameCalloutInstance);

		final MethodNameCalloutInstance methodnameCallout = (MethodNameCalloutInstance)calloutInstance;
		@SuppressWarnings("deprecation")
		final org.compiere.model.Callout legacyCallout = methodnameCallout.getLegacyCallout();

		final Class<?> calloutClassActual = legacyCallout.getClass();
		Assert.assertTrue("Callout class is not assignable from " + calloutClass + ": " + calloutClassActual,
				calloutClass.isAssignableFrom(calloutClassActual));

		Assert.assertEquals("LegacyCalloutAdapter is not using the right method: " + legacyCallout, methodName, methodnameCallout.getMethodName());
	}
}

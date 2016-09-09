package org.adempiere.ad.callout.spi.impl;

import java.util.Map.Entry;
import java.util.Properties;
import java.util.function.Supplier;

import org.adempiere.ad.callout.api.IADColumnCalloutDAO;
import org.adempiere.ad.callout.api.ICalloutInstance;
import org.adempiere.ad.callout.api.TableCalloutsMap;
import org.adempiere.ad.callout.api.impl.MethodNameCalloutInstance;
import org.adempiere.ad.callout.api.impl.RuleCalloutInstance;
import org.adempiere.ad.callout.spi.IDefaultCalloutProvider;
import org.adempiere.util.Check;
import org.adempiere.util.Services;
import org.adempiere.util.proxy.Cached;
import org.compiere.model.I_AD_ColumnCallout;
import org.compiere.model.MRule;
import org.slf4j.Logger;

import com.google.common.collect.ImmutableListMultimap;
import com.google.common.collect.ListMultimap;

import de.metas.adempiere.util.CacheCtx;
import de.metas.logging.LogManager;

/**
 * Provides {@link ICalloutInstance}s from {@link I_AD_ColumnCallout}.
 * 
 * @author metas-dev <dev@metasfresh.com>
 *
 */
class DefaultCalloutProvider implements IDefaultCalloutProvider
{
	private static final transient Logger logger = LogManager.getLogger(DefaultCalloutProvider.class);

	@Override
	public TableCalloutsMap getCallouts(final Properties ctx, final int adTableId)
	{
		final TableCalloutsMap.Builder tableCalloutsBuilder = TableCalloutsMap.builder();
		for (final Entry<Integer, Supplier<ICalloutInstance>> entry : supplyCallouts(ctx, adTableId).entries())
		{
			final Supplier<ICalloutInstance> columnCalloutSupplier = entry.getValue();
			try
			{
				final ICalloutInstance callout = columnCalloutSupplier.get();
				if (callout == null)
				{
					continue;
				}

				final int adColumnId = entry.getKey();
				tableCalloutsBuilder.put(adColumnId, callout);
			}
			catch (final Exception ex)
			{
				logger.warn("Failed creating callout instance for {}. Skipped.", columnCalloutSupplier, ex);
			}
		}

		return tableCalloutsBuilder.build();
	}

	@Cached
	public ImmutableListMultimap<Integer, Supplier<ICalloutInstance>> supplyCallouts(@CacheCtx final Properties ctx, final int adTableId)
	{
		final ListMultimap<Integer, I_AD_ColumnCallout> calloutsDef = Services.get(IADColumnCalloutDAO.class).retrieveAvailableCalloutsToRun(ctx, adTableId);
		if (calloutsDef == null || calloutsDef.isEmpty())
		{
			return ImmutableListMultimap.of();
		}

		final ImmutableListMultimap.Builder<Integer, Supplier<ICalloutInstance>> callouts = ImmutableListMultimap.builder();

		for (final Entry<Integer, I_AD_ColumnCallout> entry : calloutsDef.entries())
		{
			final I_AD_ColumnCallout calloutDef = entry.getValue();
			final Supplier<ICalloutInstance> calloutSupplier = supplyCalloutInstanceOrNull(calloutDef);
			if (calloutSupplier == null)
			{
				continue;
			}

			final int adColumnId = entry.getKey();
			callouts.put(adColumnId, calloutSupplier);
		}

		return callouts.build();
	}

	/**
	 * @return supplier or <code>null</code> but never throws exception
	 */
	private Supplier<ICalloutInstance> supplyCalloutInstanceOrNull(final I_AD_ColumnCallout calloutDef)
	{
		if (calloutDef == null)
		{
			return null;
		}
		if (!calloutDef.isActive())
		{
			return null;
		}

		try
		{
			final String calloutStr = calloutDef.getClassname();
			Check.assumeNotEmpty(calloutStr, "calloutStr is not empty");

			if (calloutStr.toLowerCase().startsWith(MRule.SCRIPT_PREFIX))
			{
				final String ruleValue = calloutStr.substring(MRule.SCRIPT_PREFIX.length());
				return RuleCalloutInstance.supplier(ruleValue);
			}
			else
			{
				return MethodNameCalloutInstance.supplier(calloutStr);
			}
		}
		catch (final Exception e)
		{
			// We are just logging and discarding the error because there is nothing that we can do about it
			// More, we want to load the other callouts and not just fail
			logger.error("Failed creating callout instance for " + calloutDef, e);
			return null;
		}
	}

}

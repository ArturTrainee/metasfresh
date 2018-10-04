/**
 * 
 */
package de.metas.contracts.order;

import static org.adempiere.model.InterfaceWrapperHelper.save;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.adempiere.ad.dao.IQueryBL;
import org.adempiere.model.InterfaceWrapperHelper;
import org.adempiere.util.proxy.Cached;
import org.springframework.stereotype.Component;

import de.metas.contracts.model.I_C_Flatrate_Term;
import de.metas.contracts.subscription.model.I_C_Order;
import de.metas.contracts.subscription.model.I_C_OrderLine;
import de.metas.order.OrderId;
import de.metas.util.Services;
import lombok.NonNull;

/*
 * #%L
 * de.metas.contracts
 * %%
 * Copyright (C) 2018 metas GmbH
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

/**
 * @author metas-dev <dev@metasfresh.com>
 *
 */
@Component
public class ContractOrderService
{
	public boolean isContractSalesOrder(@NonNull final OrderId orderId)
	{
		return Services.get(IQueryBL.class)
				.createQueryBuilder(I_C_OrderLine.class)
				.addOnlyActiveRecordsFilter()
				.addEqualsFilter(I_C_OrderLine.COLUMNNAME_C_Order_ID, orderId)
				.addNotNull(I_C_OrderLine.COLUMN_C_Flatrate_Conditions_ID)
				.create()
				.match();
	}

	@Cached(cacheName = I_C_Flatrate_Term.Table_Name + "#by#OrderId")
	public List<I_C_Flatrate_Term> retrieveFlatrateTerms(@NonNull final OrderId orderId)
	{
		return Services.get(IQueryBL.class).createQueryBuilder(I_C_OrderLine.class)
				.addOnlyActiveRecordsFilter()
				.addOnlyContextClient()
				.addEqualsFilter(I_C_OrderLine.COLUMNNAME_C_Order_ID, orderId)
				.andCollectChildren(I_C_Flatrate_Term.COLUMN_C_OrderLine_Term_ID, I_C_Flatrate_Term.class)
				.create()
				.list();
	}


	/**
	 * retrieves the linked order through column <code>I_C_Order.COLUMNNAME_Ref_FollowupOrder_ID</code>
	 * @param orderId
	 * @return
	 */
	@Cached(cacheName = I_C_Order.Table_Name + "#by#OrderId")
	public OrderId retrieveLinkedFollowUpContractOrder(@NonNull final OrderId orderId)
	{
		int oroginalOrderId = Services.get(IQueryBL.class).createQueryBuilder(I_C_Order.class)
				.addOnlyActiveRecordsFilter()
				.addOnlyContextClient()
				.addEqualsFilter(I_C_Order.COLUMNNAME_Ref_FollowupOrder_ID, orderId)
				.create()
				.firstId();

		return OrderId.ofRepoIdOrNull(oroginalOrderId);
	}

	/**
	 * retrieves recursively all orders related to a contract, inclusive the one given as parameter
	 * @param orderId
	 * @return
	 */
	public Set<OrderId> retrieveAllContractOrderList(@NonNull final OrderId orderId)
	{
		final Set<OrderId> orderIds = new HashSet<>();
		final OrderId ancestorId = retrieveOriginalContractOrder(orderId);
		if (ancestorId != null)
		{
			orderIds.add(ancestorId);
			buildAllContractOrderList(ancestorId, orderIds);
		}
		else
		{	// add itself
			orderIds.add(orderId);
		}
			

		return orderIds;
	}

	/**
	 * retrieves original order through column <code>I_C_Order.COLUMNNAME_Ref_FollowupOrder_ID</code>,
	 * going recursively until the original one
	 * 
	 * @param orderId
	 * @return OrderId
	 */
	@Cached(cacheName = I_C_Order.Table_Name + "#by#OrderId")
	public OrderId retrieveOriginalContractOrder(@NonNull final OrderId orderId)
	{
		OrderId ancestor = retrieveLinkedFollowUpContractOrder(orderId);

		if (ancestor != null)
		{
			final OrderId nextAncestor = retrieveOriginalContractOrder(ancestor);
			if (nextAncestor != null)
			{
				ancestor = nextAncestor;
			}
		}

		return ancestor;
	}

	/**
	 * builds up a list of contract orders based on column <code>I_C_Order.COLUMNNAME_Ref_FollowupOrder_ID</code>
	 * 
	 * @param orderId
	 * @param contractOrderIds
	 * @return
	 */
	private void buildAllContractOrderList(@NonNull final OrderId orderId, @NonNull Set<OrderId> contractOrderIds)
	{
		final I_C_Order order = InterfaceWrapperHelper.load(orderId, I_C_Order.class);
		final OrderId nextAncestorId = OrderId.ofRepoIdOrNull(order.getRef_FollowupOrder_ID());
		if (nextAncestorId != null)
		{
			contractOrderIds.add(nextAncestorId);
			buildAllContractOrderList(nextAncestorId, contractOrderIds);
		}
	}
	
	public I_C_Flatrate_Term retrieveTopExtendedTerm(@NonNull final I_C_Flatrate_Term term)
	{
		I_C_Flatrate_Term nextTerm = term.getC_FlatrateTerm_Next();

		if (nextTerm != null)
		{
			nextTerm = retrieveTopExtendedTerm(nextTerm);
		}

		return nextTerm == null ? term : nextTerm;
	}
	
	public void setOrderContractStatusAndSave(@NonNull final I_C_Order order, @NonNull final String contractStatus)
	{
		order.setContractStatus(contractStatus);
		save(order);
	}
}

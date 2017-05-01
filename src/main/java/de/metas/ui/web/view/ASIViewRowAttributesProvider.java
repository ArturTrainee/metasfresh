package de.metas.ui.web.view;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import de.metas.ui.web.pattribute.ASIDocument;
import de.metas.ui.web.pattribute.ASILayout;
import de.metas.ui.web.pattribute.ASIRepository;
import de.metas.ui.web.window.datatypes.DocumentId;
import lombok.NonNull;

/*
 * #%L
 * metasfresh-webui-api
 * %%
 * Copyright (C) 2017 metas GmbH
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 2 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public
 * License along with this program. If not, see
 * <http://www.gnu.org/licenses/gpl-2.0.html>.
 * #L%
 */

public class ASIViewRowAttributesProvider implements IViewRowAttributesProvider
{
	public static final ASIViewRowAttributesProvider newInstance(final ASIRepository asiRepository)
	{
		return new ASIViewRowAttributesProvider(asiRepository);
	}
	
	private final ASIRepository asiRepository;
	private final Map<DocumentId, ASIViewRowAttributes> attributesById = new ConcurrentHashMap<>();

	private ASIViewRowAttributesProvider(@NonNull final ASIRepository asiRepository)
	{
		this.asiRepository = asiRepository;
	}
	
	@Override
	public IViewRowAttributes getAttributes(final DocumentId rowId_NOTUSED, final DocumentId asiId)
	{
		return attributesById.computeIfAbsent(asiId, this::createAttributes);
	}
	
	public IViewRowAttributes getAttributesByASI(final int asiId)
	{
		return attributesById.computeIfAbsent(DocumentId.of(asiId), this::createAttributes);
	}

	private final ASIViewRowAttributes createAttributes(final DocumentId asiId)
	{
		final ASIDocument asiDoc = asiRepository.loadReadonly(asiId.toInt());
		final ASILayout asiLayout = asiDoc.getLayout();
		return new ASIViewRowAttributes(asiDoc, asiLayout);
	}

	@Override
	public void invalidateAll()
	{
		attributesById.clear();
	}

}

package de.metas.ui.web.menu.datatypes.json;

import java.io.Serializable;
import java.util.List;

import org.adempiere.util.GuavaCollectors;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.MoreObjects;
import com.google.common.collect.ImmutableList;

import de.metas.ui.web.menu.MenuNode;

/*
 * #%L
 * metasfresh-webui-api
 * %%
 * Copyright (C) 2016 metas GmbH
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

@SuppressWarnings("serial")
public final class JSONMenuNode implements Serializable
{
	public static final JSONMenuNode ofPath(final List<MenuNode> path, final boolean includeLastNode)
	{
		if (path == null || path.isEmpty())
		{
			throw new IllegalArgumentException("Invalid path");
		}

		int lastIndex = path.size() - 1;
		if(!includeLastNode)
		{
			lastIndex--;
		}

		JSONMenuNode jsonChildNode = null;
		for (int i = lastIndex; i >= 0; i--)
		{
			final MenuNode node = path.get(i);
			jsonChildNode = new JSONMenuNode(node, jsonChildNode);
		}
		return jsonChildNode;
	}

	public static JSONMenuNode of(final MenuNode node, final int childrenLimit)
	{
		final int depth = Integer.MAX_VALUE;
		return new JSONMenuNode(node, depth, childrenLimit);
	}

	public static JSONMenuNode of(final MenuNode node, final int depth, final int childrenLimit)
	{
		return new JSONMenuNode(node, depth, childrenLimit);
	}

	@JsonProperty("nodeId")
	private final String nodeId;
	@JsonProperty("caption")
	private final String caption;
	@JsonProperty("type")
	private final JSONMenuNodeType type;
	@JsonProperty("elementId")
	@JsonInclude(JsonInclude.Include.NON_NULL)
	private final Integer elementId;

	@JsonProperty("children")
	@JsonInclude(JsonInclude.Include.NON_EMPTY)
	private final List<JSONMenuNode> children;

	private JSONMenuNode(final MenuNode node, final int depth, final int childrenLimit)
	{
		super();
		nodeId = node.getId();
		caption = node.getCaption();
		type = JSONMenuNodeType.fromNullable(node.getType());
		elementId = normalizeElementId(node.getElementId());

		if (depth <= 0)
		{
			children = ImmutableList.of();
		}
		else
		{
			children = node.getChildren()
					.stream()
					.limit(childrenLimit > 0 ? childrenLimit: Long.MAX_VALUE)
					.map(childNode -> new JSONMenuNode(childNode, depth - 1, childrenLimit))
					.collect(GuavaCollectors.toImmutableList());
		}
	}

	/**
	 * Path constructor
	 *
	 * @param node
	 * @param jsonChildNode
	 */
	private JSONMenuNode(final MenuNode node, final JSONMenuNode jsonChildNode)
	{
		super();
		nodeId = node.getId();
		caption = node.getCaption();
		type = JSONMenuNodeType.fromNullable(node.getType());
		elementId = normalizeElementId(node.getElementId());
		children = jsonChildNode == null ? ImmutableList.of() : ImmutableList.of(jsonChildNode);
	}

	private static final Integer normalizeElementId(final int elementId)
	{
		return elementId <= 0 ? null : elementId;
	}

	@JsonCreator
	private JSONMenuNode(
			@JsonProperty("nodeId") final String nodeId //
			, @JsonProperty("caption") final String caption //
			, @JsonProperty("type") final JSONMenuNodeType type //
			, @JsonProperty("elementId") final Integer elementId //
			, @JsonProperty("children") final List<JSONMenuNode> children //
	)
	{
		super();
		this.nodeId = nodeId;
		this.caption = caption;
		this.type = type;
		this.elementId = elementId;
		this.children = children == null ? ImmutableList.of() : ImmutableList.copyOf(children);
	}

	@Override
	public String toString()
	{
		return MoreObjects.toStringHelper(this)
				.add("nodeId", nodeId)
				.add("caption", caption)
				.add("type", type)
				.add("elementId", elementId)
				.toString();
	}

	public String getNodeId()
	{
		return nodeId;
	}

	public String getCaption()
	{
		return caption;
	}

	public JSONMenuNodeType getType()
	{
		return type;
	}

	public Integer getElementId()
	{
		return elementId;
	}

	public List<JSONMenuNode> getChildren()
	{
		return children;
	}
}

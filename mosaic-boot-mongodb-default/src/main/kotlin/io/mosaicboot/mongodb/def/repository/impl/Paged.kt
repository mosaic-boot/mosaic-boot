/**
 * Copyright 2025 JC-Lab (mosaicboot.io)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.mosaicboot.mongodb.def.repository.impl

import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.Pageable
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.aggregate
import org.springframework.data.mongodb.core.aggregation.*
import org.springframework.data.mongodb.core.query.Criteria

interface Paged<T> {
    val total: Long
    val items: List<T>
}

fun <P : Paged<T>, T> MongoTemplate.pagedAggregation(
    pageable: Pageable,
    aggregation: Aggregation,
    inputClass: Class<*>,
    outputClass: Class<P>,
): Page<T> {
    val result = this.aggregate(
        aggregation.pagination(pageable),
        inputClass,
        outputClass
    )
    val output = result.uniqueMappedResult!!
    return PageImpl(output.items, pageable, output.total)
}

fun Aggregation.pagination(pageable: Pageable): Aggregation {
    val skipSize = (pageable.pageSize * pageable.pageNumber).toLong()
    val limitSize = pageable.pageSize.toLong()
    val pageFacetOperation =
        Aggregation.facet(
            SkipOperation(skipSize),
            LimitOperation(limitSize),
        ).`as`("items")
            .and(CountOperation("total")).`as`("total")
    val pageProjectOperation = Aggregation.project("items")
        .and(
            ConditionalOperators.Cond.`when`(Criteria.where("total").ne(emptyList<String>()))
                .then(ArrayOperators.ArrayElemAt.arrayOf("total.total").elementAt(0))
                .otherwise(0)
        ).`as`("total")
    this.pipeline.add(pageFacetOperation)
    this.pipeline.add(pageProjectOperation)
    return this
}

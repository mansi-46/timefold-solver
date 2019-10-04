/*
 * Copyright 2019 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.optaplanner.core.impl.score.stream.drools.bi;

import java.util.function.BiPredicate;

import org.drools.model.Declaration;
import org.drools.model.PatternDSL;
import org.optaplanner.core.impl.score.stream.drools.DroolsConstraintFactory;

public class DroolsFilterBiConstraintStream<Solution_, A, B> extends DroolsAbstractBiConstraintStream<Solution_, A, B> {

    private final BiPredicate<A, B> biPredicate;
    private final PatternDSL.PatternDef<B> rightPattern;

    public DroolsFilterBiConstraintStream(DroolsConstraintFactory<Solution_> constraintFactory,
            DroolsAbstractBiConstraintStream<Solution_, A, B> parent, BiPredicate<A, B> biPredicate) {
        super(constraintFactory, parent);
        this.biPredicate = biPredicate;
        this.rightPattern = parent.getRightPattern().expr(getLeftVariableDeclaration(), (b, a) -> biPredicate.test(a, b));
    }

    @Override
    public Declaration<A> getLeftVariableDeclaration() {
        return parent.getLeftVariableDeclaration();
    }

    @Override
    public PatternDSL.PatternDef<A> getLeftPattern() {
        return parent.getLeftPattern();
    }

    @Override
    public Declaration<B> getRightVariableDeclaration() {
        return parent.getRightVariableDeclaration();
    }

    @Override
    public PatternDSL.PatternDef<B> getRightPattern() {
        return rightPattern;
    }

    @Override
    public String toString() {
        return "BiFilter() with " + childStreamList.size() + " children";
    }
}
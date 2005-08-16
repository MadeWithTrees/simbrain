/*
 * Part of Simbrain--a java-based neural network kit
 * Copyright (C) 2005 Jeff Yoshimi <www.jeffyoshimi.net>
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of 
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
package org.simnet.synapses;

import java.util.ArrayList;

import org.simnet.interfaces.Neuron;
import org.simnet.interfaces.Synapse;
import org.simnet.util.SMath;


public class SubtractiveNormalizationSynapse extends Synapse {
	
	private double momentum = 1;
	
	
	public SubtractiveNormalizationSynapse() {
	}
	
	public SubtractiveNormalizationSynapse(Synapse s) {
		super(s);
	}
	
	public static String getName() {return "Subtractive Normalizaion";}

	public Synapse duplicate() {
//		Hebbian h = new Hebbian();
		return null;
	}
	
	/**
	 * Creates a weight connecting source and target neurons
	 * 
	 * @param source source neuron
	 * @param target target neuron
	 */
	public SubtractiveNormalizationSynapse(Neuron source, Neuron target) {
		this.source = source;
		this.target = target;
	}

	public void update() {
		
		double input = getSource().getActivation();
		double output = getTarget().getActivation();
		double averageInput = getTarget().getAverageInput();
		
		strength += momentum * (output * input - (output * averageInput));
		strength = clip(strength);
	}
		
	
	/**
	 * @return Returns the momentum.
	 */
	public double getMomentum() {
		return momentum;
	}
	/**
	 * @param momentum The momentum to set.
	 */
	public void setMomentum(double momentum) {
		this.momentum = momentum;
	}
}

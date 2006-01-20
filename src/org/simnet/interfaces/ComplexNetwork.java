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
package org.simnet.interfaces;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import org.simbrain.workspace.Workspace;


/**
 * <b>ComplexNetwok</b> contains lists of sub-networks, e.g. backprop, where the subnetworks are "layers"
 */
public abstract class ComplexNetwork extends Network {
    /** Array list of networks. */
    protected ArrayList networkList = new ArrayList();

    /**
     * Initializes complex network.
     */
    public void init() {
        super.init();

        for (int i = 0; i < networkList.size(); i++) {
            ((Network) networkList.get(i)).init();
            ((Network) networkList.get(i)).setNetworkParent(this);
        }
    }

    /**
     * The core update function of the neural network.  Calls the current update function on each neuron, decays all
     * the neurons, and checks their bounds.
     */
    public void update() {
        updateAllNetworks();
    }

    /**
     * Updates all networks.
     */
    public void updateAllNetworks() {
        Iterator i = networkList.iterator();

        while (i.hasNext()) {
            ((Network) i.next()).update();
        }
    }

    /**
     * Adds a new network.
     * @param n Network type to add.
     */
    public void addNetwork(final Network n) {
        networkList.add(n);
        n.setNetworkParent(this);
        fireSubnetAdded(n);
    }

    /**
     * @param i Network number to get.
     * @return network
     */
    public Network getNetwork(final int i) {
        return (Network) networkList.get(i);
    }

    /**
     * Debug networks.
     * @return String
     */
    public String toString() {
        String ret = super.toString();

        for (int i = 0; i < networkList.size(); i++) {
            Network net = (Network) networkList.get(i);
            ret += ("\n" + getIndents() + "Sub-network " + (i + 1) + " (" + net.getType() + ")");
            ret += (getIndents() + "--------------------------------\n");
            ret += net.toString();
        }
        return ret;
    }

    /**
     * Delete network, and any of its ancestors which thereby become empty.
     * @param toDelete Network to be deleted
     */
    public void deleteNetwork(final Network toDelete) {
        networkList.remove(toDelete);

        //If this is the last network in a subnetwork, remove the subnetwork
        if (networkList.size() == 0) {
            ComplexNetwork parent = (ComplexNetwork) getNetworkParent();

            if (parent != null) {
                parent.deleteNetwork(this);
            }
        }
        fireSubnetDeleted(toDelete);
    }

    /**
     * Delete neuron, and any of its ancestors which thereby become empty.
     * @param toDelete Neuron to be deleted
     * @param notify Notify listeners that a neuron is being deleted
     */
    public void deleteNeuron(final Neuron toDelete, final boolean notify) {
        //If this is a top-level neuron use the regular delete; if it is a neuron in a sub-net, use its parent's delete
        if (this == toDelete.getParentNetwork()) {
            super.deleteNeuron(toDelete, notify);
        } else {
            toDelete.getParentNetwork().deleteNeuron(toDelete, notify);
        }

        //The subnetwork "parent" this neuron is part of is empty, so remove it from the grandparent network
        Network parent = toDelete.getParentNetwork();

        if (parent.getNeuronCount() == 0) {
            ComplexNetwork grandParent = (ComplexNetwork) parent.getNetworkParent();

            if (grandParent != null) {
                grandParent.deleteNetwork(parent);
            }
        }
    }

    /**
     * Add an array of networks and set their parents to this.
     *
     * @param networks list of neurons to add
     */
    public void addNetworkList(final ArrayList networks) {
        for (int i = 0; i < networks.size(); i++) {
            Network n = (Network) networks.get(i);
            addNetwork(n);
        }
    }

    /**
     * @return Returns the networkList.
     */
    public ArrayList getNetworkList() {
        return networkList;
    }

    /**
     * @param networkList The networkList to set.
     */
    public void setNetworkList(final ArrayList networkList) {
        this.networkList = networkList;
    }

    /**
     * Create "flat" list of neurons, which includes the top-level neurons plus all subnet neurons.
     *
     * @return the flat llist
     */
    public ArrayList getFlatNeuronList() {
        ArrayList ret = new ArrayList();
        ret.addAll(neuronList);

        for (int i = 0; i < networkList.size(); i++) {
            Network net = (Network) networkList.get(i);
            ArrayList toAdd;

            if (net instanceof ComplexNetwork) {
                toAdd = (ArrayList) ((ComplexNetwork) net).getFlatNeuronList();
            } else {
                toAdd = (ArrayList) ((Network) networkList.get(i)).getNeuronList();
            }

            ret.addAll(toAdd);
        }

        return ret;
    }

    /**
     * Create "flat" list of synapses, which includes the top-level synapses plus all subnet synapses.
     *
     * @return the flat list
     */
    public ArrayList getFlatSynapseList() {
        ArrayList ret = new ArrayList();
        ret.addAll(weightList);

        for (int i = 0; i < networkList.size(); i++) {
            Network net = (Network) networkList.get(i);
            ArrayList toAdd;

            if (net instanceof ComplexNetwork) {
                toAdd = (ArrayList) ((ComplexNetwork) net).getFlatSynapseList();
            } else {
                toAdd = (ArrayList) ((Network) networkList.get(i)).getWeightList();
            }

            ret.addAll(toAdd);
        }

        return ret;
    }

    /**
     * Update all ids. Used in for persistences before writing xml file.
     */
    public void updateIds() {

        setId("root_net");

        // Update neteworkids
        int netIndex = 1;
        for (Iterator networks = getNetworkList().iterator(); networks.hasNext(); netIndex++) {
            Network network = (Network) networks.next();
            network.setId("net_" + netIndex);
        }

        // Update neuron ids
        int nIndex = 1;
        for (Iterator neurons = getFlatNeuronList().iterator(); neurons.hasNext(); nIndex++) {
            Neuron neuron = (Neuron) neurons.next();
            neuron.setId("n_" + nIndex);
        }

        // Update synapse ids
        int sIndex = 1;
        for (Iterator synapses = getFlatSynapseList().iterator(); synapses.hasNext(); sIndex++) {
            Synapse synapse = (Synapse) synapses.next();
            synapse.setId("s_" + sIndex);
        }
    }
    
    /**
     * Returns all Input Neurons.
     *
     * @return list of input neurons;
     */
    public Collection getInputNeurons() {
        ArrayList inputs = new ArrayList();
        for (Iterator i = this.getFlatNeuronList().iterator(); i.hasNext();) {
            Neuron neuron = (Neuron) i.next();
            if (neuron.isInput()) {
                inputs.add(neuron);
            }
        }
        return inputs;
    }

    /**
     * Returns all Output Neurons.
     *
     * @return list of output neurons;
     */
    public Collection getOutputNeurons() {
        ArrayList outputs = new ArrayList();
        for (Iterator i = this.getFlatNeuronList().iterator(); i.hasNext();) {
            Neuron neuron = (Neuron) i.next();
            if (neuron.isOutput()) {
                outputs.add(neuron);
            }
        }
        return outputs;
    }

    /**
     * Fire a subnetwork added event to all registered model listeners.
     *
     * @param added synapse which was added
     */
    public void fireSubnetAdded(final Network added) {
        for (Iterator i = listenerList.iterator(); i.hasNext();) {
            NetworkListener listener = (NetworkListener) i.next();
            listener.subnetAdded(new NetworkEvent(this, added));
        }
    }

    /**
     * Fire a subnetwork deleted event to all registered model listeners.
     *
     * @param deleted synapse which was deleted
     */
    public void fireSubnetDeleted(final Network deleted) {
        for (Iterator i = listenerList.iterator(); i.hasNext();) {
            NetworkListener listener = (NetworkListener) i.next();
            listener.subnetRemoved(new NetworkEvent(this, deleted));
        }
    }

    /**
     * Add the specified network listener.
     *
     * @param l listener to add
     */
    public void addNetworkListener(final NetworkListener l) {
        listenerList.add(l);
        for (Iterator networks = networkList.iterator(); networks.hasNext();) {
            Network net = (Network) networks.next();
            net.addNetworkListener(l);
        }
    }

    /**
     * Remove the specified network listener.
     *
     * @param l listener to remove
     */
    public void removeNetworkListener(final NetworkListener l) {
        listenerList.remove(l);
        for (Iterator networks = networkList.iterator(); networks.hasNext();) {
            Network net = (Network) networks.next();
            net.removeNetworkListener(l);
        }
    }

}

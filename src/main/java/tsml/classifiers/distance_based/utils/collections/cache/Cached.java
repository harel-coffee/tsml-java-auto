/*
 * This file is part of the UEA Time Series Machine Learning (TSML) toolbox.
 *
 * The UEA TSML toolbox is free software: you can redistribute it and/or 
 * modify it under the terms of the GNU General Public License as published 
 * by the Free Software Foundation, either version 3 of the License, or 
 * (at your option) any later version.
 *
 * The UEA TSML toolbox is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with the UEA TSML toolbox. If not, see <https://www.gnu.org/licenses/>.
 */
 
package tsml.classifiers.distance_based.utils.collections.cache;

/**
 * Purpose: // todo - docs - type the purpose of the code here
 * <p>
 * Contributors: goastler
 */

public abstract class Cached {

    private boolean read = true;
    private boolean write = true;

    public boolean isWrite() {
        return write;
    }

    public Cached setWrite(final boolean write) {
        this.write = write;
        return this;
    }

    public boolean isRead() {
        return read;
    }

    public Cached setRead(final boolean read) {
        this.read = read;
        return this;
    }

    public boolean isReadOrWrite() {
        return isWrite() || isRead();
    }

    public boolean isReadAndWrite() {
        return isWrite() && isRead();
    }

    public boolean isReadOnly() {
        return isRead() && !isWrite();
    }

    public boolean isWriteOnly() {
        return !isRead() && isWrite();
    }

    public boolean isOff() {
        return !isRead() && !isWrite();
    }
}

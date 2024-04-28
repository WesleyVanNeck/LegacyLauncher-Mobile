/*
 * Copyright (C) 2013 The Android Open Source Project
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

package android.util;

import java.util.Iterator;
import java.util.Map;
import java.util.Objects;

/**
 * Helper for writing standard Java collection interfaces to a data
 * structure like {@link ArrayMap}.
 * @hide
 *
 * @param <K> the key type
 * @param <V> the value type
 */
public abstract class MapCollections<K, V> {
    EntrySet<K, V> mEntrySet;
    KeySet<K> mKeySet;
    ValuesCollection<V> mValues;

    final class ArrayIterator<T> implements Iterator<T> {
        final int mOffset;
        int mSize;
        int mIndex;
        boolean mCanRemove = false;

        ArrayIterator(int offset) {
            mOffset = offset;
            mSize = getColSize();
        }

        @Override
        public boolean hasNext() {
            return mIndex < mSize;
        }

        @Override
        public T next() {
            Object res = getColEntry(mIndex, mOffset);
            mIndex++;
            mCanRemove = true;
            return (T) res;
        }

        @Override
        public void remove() {
            if (!mCanRemove) {
                throw new IllegalStateException();
            }
            mIndex--;
            mSize--;
            mCanRemove = false;
            removeAtCol(mIndex);
        }
    }

    final class MapIterator implements Iterator<Map.Entry<K, V>>, Map.Entry<K, V> {
        int mEnd;
        int mIndex;
        boolean mEntryValid = false;

        MapIterator() {
            mEnd = getColSize() - 1;
            mIndex = -1;
        }

        @Override
        public boolean hasNext() {
            return mIndex < mEnd;
        }

        @Override
        public Map.Entry<K, V> next() {
            mIndex++;
            mEntryValid = true;
            return this;
        }

        @Override
        public void remove() {
            if (!mEntryValid) {
                throw new IllegalStateException();
            }
            removeAtCol(mIndex);
            mIndex--;
            mEnd--;
            mEntryValid = false;
        }

        @Override
        public K getKey() {
            if (!mEntryValid) {
                throw new IllegalStateException(
                        "This container does not support retaining Map.Entry objects");
            }
            return (K) getColEntry(mIndex, 0);
        }

        @Override
        public V getValue() {
            if (!mEntryValid) {
                throw new IllegalStateException(
                        "This container does not support retaining Map.Entry objects");
            }
            return (V) getColEntry(mIndex, 1);
        }

        @Override
        public V setValue(V object) {
            if (!mEntryValid) {
                throw new IllegalStateException(
                        "This container does not support retaining Map.Entry objects");
            }
            return setValueAtCol(mIndex, object);
        }

        @Override
        public final boolean equals(Object o) {
            if (!mEntryValid) {
                throw new IllegalStateException(
                        "This container does not support retaining Map.Entry objects");
            }
            if (!(o instanceof Map.Entry)) {
                return false;
            }
            Map.Entry<?, ?> e = (Map.Entry<?, ?>) o;
            return Objects.equal(e.getKey(), getColEntry(mIndex, 0))
                    && Objects.equal(e.getValue(), getColEntry(mIndex, 1));
        }

        @Override
        public final int hashCode() {
            if (!mEntryValid) {
                throw new IllegalStateException(
                        "This container does not support retaining Map.Entry objects");
            }
            final Object key = getColEntry(mIndex, 0);
            final Object value = getColEntry(mIndex, 1);
            return (key == null ? 0 : key.hashCode()) ^
                    (value == null ? 0 : value.hashCode());
        }

        @Override
        public final String toString() {
            return getKey() + "=" + getValue();
        }
    }

    final class EntrySet<K, V> implements Set<Map.Entry<K, V>> {
        @Override
        public boolean add(Map.Entry<K, V> object) {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean addAll(Collection<? extends Map.Entry<K, V>> collection) {
            int oldSize = getColSize();
            for (Map.Entry<K, V> entry : collection) {
                put(entry.getKey(), entry.getValue());
            }
            return oldSize != getColSize();
        }

        @Override
        public void clear() {
            clearCol();
        }

        @Override
        public boolean contains(Object o) {
            if (!(o instanceof Map.Entry))
                return false;
            Map.Entry<?, ?> e = (Map.Entry<?, ?>) o;
            int index = indexOfKeyCol(e.getKey());
            if (index < 0) {
                return false;
            }
            Object foundVal = getColEntry(index, 1);
            return Objects.equal(foundVal, e.getValue());
        }

        @Override
        public boolean containsAll(Collection<?> collection) {
            Iterator<?> it = collection.iterator();
            while (it.hasNext()) {
                if (!contains(it.next())) {
                    return false;
                }
            }
            return true;
        }

        @Override
        public boolean isEmpty() {
            return getColSize() == 0;
        }

        @Override
        public Iterator<Map.Entry<K, V>> iterator() {
            return new MapIterator();
        }

        @Override
        public boolean remove(Object object) {
            if (!(object instanceof Map.Entry)) {
                return false;
            }
            Map.Entry<?, ?> entry = (Map.Entry<?, ?>) object;
            int index = indexOfKeyCol(entry.getKey());
            if (index < 0) {
                return false;
            }
            if (!Objects.equal(getColEntry(index, 1), entry.getValue())) {
                return false;
            }
            removeAtCol(index);
            return true;
        }

        @Override
        public boolean removeAll(Collection<?> collection) {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean retainAll(Collection<?> collection) {
            throw new UnsupportedOperationException();
        }

        @Override
        public int size() {
            return getColSize();
        }

        @Override
        public Object[] toArray() {
            throw new UnsupportedOperationException();
        }

        @Override
        public <T> T[] toArray(T[] array) {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean equals(Object object) {
            if (object == this) {
                return true;
            }
            if (!(object instanceof Set)) {
                return false;
            }
            Set<?> set = (Set<?>) object;
            if (set.size() != getColSize()) {
                return false;
            }
            for (Map.Entry<K, V> entry : this) {
                if (!set.contains(entry)) {
                    return false;
                }
            }
            return true;
        }
    }

    final class KeySet<K> implements Set<K> {

        @Override
        public boolean add(K object) {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean addAll(Collection<? extends K> collection) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void clear() {
            clearCol();
        }

        @Override
        public boolean contains(Object object) {
            return indexOfKeyCol(object) >= 0;
        }

        @Override
        public boolean containsAll(Collection<?> collection) {
            for (Object obj : collection) {
                if (!contains(obj)) {
                    return false;
               

/*
 * Copyright (c) 2017-2017 the original author or authors.
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
package org.eclipse.jetty.reactive.client.internal;

import java.nio.ByteBuffer;
import java.util.Iterator;

import org.eclipse.jetty.client.api.ContentProvider;
import org.eclipse.jetty.client.util.DeferredContentProvider;
import org.eclipse.jetty.reactive.client.ContentChunk;
import org.eclipse.jetty.reactive.client.ReactiveRequest;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

public class PublisherContentProvider implements ContentProvider.Typed, Subscriber<ContentChunk> {
    private final DeferredContentProvider provider = new DeferredContentProvider();
    private final ReactiveRequest.Content content;
    private Subscription subscription;

    public PublisherContentProvider(ReactiveRequest.Content content) {
        this.content = content;
    }

    @Override
    public long getLength() {
        return content.getLength();
    }

    @Override
    public String getContentType() {
        return content.getContentType();
    }

    @Override
    public Iterator<ByteBuffer> iterator() {
        content.subscribe(this);
        return provider.iterator();
    }

    @Override
    public void onSubscribe(Subscription subscription) {
        this.subscription = subscription;
        subscription.request(1);
    }

    @Override
    public void onNext(ContentChunk chunk) {
        provider.offer(chunk.buffer, chunk.callback);
        subscription.request(1);
    }

    @Override
    public void onError(Throwable failure) {
        // TODO: what here ?
    }

    @Override
    public void onComplete() {
        provider.close();
    }
}
package com.xzentry.shorten.data.local

import com.xzentry.shorten.data.local.entity.PostEntity
import com.xzentry.shorten.data.local.entity.SourceEntity
import com.xzentry.shorten.data.local.entity.TopicEntity
import com.xzentry.shorten.data.remote.model.Post
import com.xzentry.shorten.data.remote.model.Source
import com.xzentry.shorten.data.remote.model.Topic

fun Post.toPostEntity(): PostEntity {

    return PostEntity(
        this.subTitle,
        this.imageUrl,
        this.id,
        this.source?.toSourceEntity(),
        this.title,
        this.category?.toTopicEntity(),
        this.content,
        this.sourceUrl,
        this.notification,
        this.updatedAt,
        this.videoUrl,
        this.commentsCount
    )
}

fun Source.toSourceEntity(): SourceEntity {

    return SourceEntity(
        this.sourceId,
        this.source
    )
}

fun Topic.toTopicEntity(): TopicEntity {

    return TopicEntity(
        this.catImageUrl,
        this.catId,
        this.category,
        this.topicUpdatedAt
    )
}

fun PostEntity.toPostEntity(): Post {

    return Post(
        this.subTitle,
        this.imageUrl,
        this.id,
        this.source?.toSource(),
        this.title,
        this.category?.toTopic(),
        this.content,
        this.sourceUrl,
        this.notification,
        this.updatedAt,
        this.videoUrl,
        this.commentsCount
    )
}

fun SourceEntity.toSource(): Source {

    return Source(
        this.sourceId,
        this.source
    )
}

fun TopicEntity.toTopic(): Topic{

    return Topic(
        this.catImageUrl,
        this.catId,
        this.category,
        this.topicUpdatedAt
    )
}

fun List<Topic>?.toTopicEntityList(): List<TopicEntity> {
    val topicEntityList = mutableListOf<TopicEntity>()
    this?.forEach {topic ->
        topicEntityList.add(topic.toTopicEntity())
    }
    return  topicEntityList.toList()
}

fun List<Post>?.toPostEntityList(): List<PostEntity> {
    val postEntityList = mutableListOf<PostEntity>()
    this?.forEach {post ->
        postEntityList.add(post.toPostEntity())
    }
    return  postEntityList.toList()
}

fun List<TopicEntity>?.toTopicList(): List<Topic> {
    val topicList = mutableListOf<Topic>()
    this?.forEach {topic ->
        topicList.add(topic.toTopic())
    }
    return  topicList.toList()
}

fun List<PostEntity>?.toPostList(): List<Post> {
    val postList = mutableListOf<Post>()
    this?.forEach {post ->
        postList.add(post.toPostEntity())
    }
    return  postList.toList()
}
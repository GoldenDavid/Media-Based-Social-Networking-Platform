package com.socialnetwork.profile.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(indexName = "profiles")
public class ProfileDocument {

    @Id
    private String id; // Use String for Elasticsearch ID, but will store the Profile's Integer ID as String

    @Field(type = FieldType.Keyword)
    private String userId;

    @Field(type = FieldType.Text, analyzer = "standard")
    private String displayName;

    @Field(type = FieldType.Text, analyzer = "standard")
    private String username;

    @Field(type = FieldType.Text)
    private String bio;

    @Field(type = FieldType.Keyword, index = false)
    private String profileImageUrl;
}

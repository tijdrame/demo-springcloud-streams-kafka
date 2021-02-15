package com.kafka.api.entities;

import java.util.Date;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class PageEvent {

	private String name;
	private String user;
	private Date date;
	private Integer duration;
}

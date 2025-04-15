/* generated using openapi-typescript-codegen -- do not edit */
/* istanbul ignore file */
/* tslint:disable */
/* eslint-disable */
import type { HotQuestionVO } from "./HotQuestionVO";
import type { OrderItem } from "./OrderItem";
export type Page_HotQuestionVO_ = {
  countId?: string;
  current?: number;
  maxLimit?: number;
  optimizeCountSql?: boolean;
  orders?: Array<OrderItem>;
  pages?: number;
  records?: Array<HotQuestionVO>;
  searchCount?: boolean;
  size?: number;
  total?: number;
};

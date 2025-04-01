export const queryKeys = {
  users: {
    all: ["users"] as const,
    me: () => [...queryKeys.users.all, "me"] as const,
  },
};

export type QueryKeys = typeof queryKeys;
export type UserQueryKeys = QueryKeys["users"];

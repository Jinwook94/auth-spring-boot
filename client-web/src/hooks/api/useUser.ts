import { useQuery } from "@tanstack/react-query";
import { getCurrentUser } from "@/services/userService";
import { User } from "@/types/user";
import { queryKeys } from "@/api/queryKeys.ts";

export function useCurrentUser() {
  return useQuery<User, Error>({
    queryKey: queryKeys.users.me(),
    queryFn: getCurrentUser,
    retry: 1,
    staleTime: 5 * 60 * 1000,
  });
}
